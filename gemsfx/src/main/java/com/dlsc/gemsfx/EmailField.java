package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.EmailFieldSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.CustomTextField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * EmailField is a custom control for inputting and validating email addresses.
 * It extends the base Control class and provides additional functionalities:
 * <p>
 * 1. Automatic email domain suggestions to enhance user experience. <br>
 * 2. Email address format validation to ensure input validity. <br>
 * 3. Customizable properties to control the visibility of user interface elements,
 * such as mail and validation icons, according to specific user interface requirements.
 */
public class EmailField extends Control {

    private static final boolean DEFAULT_SHOW_MAIL_ICON = true;
    private static final boolean DEFAULT_SHOW_VALIDATION_ICON = true;
    private static final boolean DEFAULT_AUTO_DOMAIN_COMPLETION_ENABLED = true;

    private static final PseudoClass VALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("valid");
    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");

    // Define the email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private final CustomTextField editor = new CustomTextField() {
        @Override
        public String getUserAgentStylesheet() {
            return Objects.requireNonNull(EmailField.class.getResource("email-field.css")).toExternalForm();
        }
    };

    public EmailField() {
        getStyleClass().add("email-field");

        setFocusTraversable(false);

        focusedProperty().addListener(it -> {
            if (isFocused()) {
                getEditor().requestFocus();
            }
        });

        valid.bind(Bindings.createBooleanBinding(() -> {
            if (isRequired()) {
                return isValidEmail(getEmailAddress());
            }
            return StringUtils.isBlank(getEmailAddress()) || isValidEmail(getEmailAddress());
        }, emailAddressProperty(), requiredProperty()));

        updateValidPseudoClass(false);

        valid.getReadOnlyProperty().addListener((ob, ov, newValue) -> updateValidPseudoClass(newValue));
    }

    public EmailField(String emailAddress) {
        this();
        setEmailAddress(emailAddress);
    }

    private void updateValidPseudoClass(Boolean isValid) {
        pseudoClassStateChanged(VALID_PSEUDO_CLASS, isValid);
        pseudoClassStateChanged(INVALID_PSEUDO_CLASS, !isValid);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EmailFieldSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(EmailField.class.getResource("email-field.css")).toExternalForm();
    }

    public final CustomTextField getEditor() {
        return editor;
    }

    // domainList

    private final ListProperty<String> domainList = new SimpleListProperty<>(this, "domainList",
            FXCollections.observableArrayList("gmail.com", "yahoo.com", "outlook.com", "hotmail.com",
                    "icloud.com", "aol.com", "mail.com", "protonmail.com", "gmx.com", "zoho.com", "qq.com",
                    "163.com", "126.com", "yeah.net", "msn.com", "live.com", "me.com"));

    public final ObservableList<String> getDomainList() {
        return domainList.get();
    }

    public final ListProperty<String> domainListProperty() {
        return domainList;
    }

    public final void setDomainList(ObservableList<String> domainList) {
        this.domainList.set(domainList);
    }

    // autoDomainCompletionEnabled

    private BooleanProperty autoDomainCompletionEnabled;

    public final boolean getAutoDomainCompletionEnabled() {
        return autoDomainCompletionEnabled == null ? DEFAULT_AUTO_DOMAIN_COMPLETION_ENABLED : autoDomainCompletionEnabled.get();
    }

    /**
     * Property for enabling or disabling the auto-completion of email domains.
     *
     * @return The BooleanProperty representing the state of auto domain completion.
     */
    public final BooleanProperty autoDomainCompletionEnabledProperty() {
        if (autoDomainCompletionEnabled == null) {
            autoDomainCompletionEnabled = new SimpleBooleanProperty(this, "autoDomainCompletionEnabled", DEFAULT_AUTO_DOMAIN_COMPLETION_ENABLED);
        }
        return autoDomainCompletionEnabled;
    }

    public final void setAutoDomainCompletionEnabled(boolean autoDomainCompletionEnabled) {
        autoDomainCompletionEnabledProperty().set(autoDomainCompletionEnabled);
    }

    // domainListCellFactory

    private ObjectProperty<Callback<ListView<String>, ListCell<String>>> domainListCellFactory;

    public final Callback<ListView<String>, ListCell<String>> getDomainListCellFactory() {
        return domainListCellFactory == null ? null : domainListCellFactory.get();
    }

    /**
     * Returns the property for the domain list cell factory.
     * This property can be used to customize the rendering of the domain suggestions in the ListView.
     *
     * @return The ObjectProperty representing the domain list cell factory.
     */
    public final ObjectProperty<Callback<ListView<String>, ListCell<String>>> domainListCellFactoryProperty() {
        if (domainListCellFactory == null) {
            domainListCellFactory = new SimpleObjectProperty<>(this, "domainListCellFactory");
        }
        return domainListCellFactory;
    }

    public final void setDomainListCellFactory(Callback<ListView<String>, ListCell<String>> cellFactory) {
        domainListCellFactoryProperty().set(cellFactory);
    }

    // required

    private final BooleanProperty required = new SimpleBooleanProperty(this, "required", false);

    public final boolean isRequired() {
        return required.get();
    }

    public final BooleanProperty requiredProperty() {
        return required;
    }

    public final void setRequired(boolean required) {
        this.required.set(required);
    }

    // prompt text support

    private final StringProperty promptText = new SimpleStringProperty(this, "promptText");

    public final String getPromptText() {
        return promptText.get();
    }

    public final StringProperty promptTextProperty() {
        return promptText;
    }

    public final void setPromptText(String promptText) {
        this.promptText.set(promptText);
    }

    // email address support

    private final StringProperty emailAddress = new SimpleStringProperty(this, "emailAddress");

    public final String getEmailAddress() {
        return emailAddress.get();
    }

    public final StringProperty emailAddressProperty() {
        return emailAddress;
    }

    public final void setEmailAddress(String emailAddress) {
        this.emailAddress.set(emailAddress);
    }

    // valid support

    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid");

    public final boolean isValid() {
        return valid.get();
    }

    public final ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }

    // Property for the tooltip text displayed when hovering over the icon indicating an invalid email address.
    private final StringProperty invalidText = new SimpleStringProperty(this, "invalidText", "Email address is invalid.");

    /**
     * Retrieves the tooltip text displayed when the email address validation fails and the user hovers over the invalid icon.
     *
     * @return Tooltip text for an invalid email address.
     */
    public final String getInvalidText() {
        return invalidText.get();
    }

    /**
     * Property for changing the tooltip text, which is displayed when hovering over the invalid icon after email address validation fails.
     *
     * @return The StringProperty for the tooltip text of an invalid email address.
     */
    public final StringProperty invalidTextProperty() {
        return invalidText;
    }

    /**
     * Sets the tooltip text that appears when the user hovers over the icon indicating the email address is invalid.
     *
     * @param invalidText The tooltip text to set for an invalid email address.
     */
    public final void setInvalidText(String invalidText) {
        this.invalidText.set(invalidText);
    }

    // Styleable property to control the visibility of the mail icon.
    private final StyleableBooleanProperty showMailIcon = new SimpleStyleableBooleanProperty(
            StyleableProperties.SHOW_MAIL_ICON, EmailField.this, "showMailIcon", DEFAULT_SHOW_MAIL_ICON);

    /**
     * Returns true if the mail icon is visible, otherwise false.
     *
     * @return The mail icon visibility
     */
    public final boolean isShowMailIcon() {
        return showMailIcon.get();
    }

    /**
     * Property for handling the mail icon visibility.
     */
    public final BooleanProperty showMailIconProperty() {
        return showMailIcon;
    }

    /**
     * Sets the visibility of the mail icon.
     *
     * @param showMailIcon true if the mail icon should be visible, otherwise false
     */
    public final void setShowMailIcon(boolean showMailIcon) {
        this.showMailIcon.set(showMailIcon);
    }

    // Styleable property to control the visibility of the validation icon.
    private final StyleableBooleanProperty showValidationIcon = new SimpleStyleableBooleanProperty(
            StyleableProperties.SHOW_VALIDATION_ICON, EmailField.this, "showValidationIcon", DEFAULT_SHOW_VALIDATION_ICON);

    /**
     * Returns true if the validation icon is visible, otherwise false.
     *
     * @return The validation icon visibility
     */
    public final boolean isShowValidationIcon() {
        return showValidationIcon.get();
    }

    /**
     * Property for handling the validation icon visibility.
     */
    public final BooleanProperty showValidationIconProperty() {
        return showValidationIcon;
    }

    /**
     * Sets the visibility of the validation icon.
     *
     * @param showValidationIcon true if the validation icon should be visible, otherwise false
     */
    public final void setShowValidationIcon(boolean showValidationIcon) {
        this.showValidationIcon.set(showValidationIcon);
    }

    private static class StyleableProperties {

        private static final CssMetaData<EmailField, Boolean> SHOW_MAIL_ICON = new CssMetaData<>(
                "-fx-show-mail-icon", BooleanConverter.getInstance(), DEFAULT_SHOW_MAIL_ICON) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(EmailField control) {
                return (StyleableProperty<Boolean>) control.showMailIconProperty();
            }

            @Override
            public boolean isSettable(EmailField control) {
                return !control.showMailIcon.isBound();
            }
        };

        private static final CssMetaData<EmailField, Boolean> SHOW_VALIDATION_ICON = new CssMetaData<>(
                "-fx-show-validation-icon", BooleanConverter.getInstance(), DEFAULT_SHOW_VALIDATION_ICON) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(EmailField control) {
                return (StyleableProperty<Boolean>) control.showValidationIconProperty();
            }

            @Override
            public boolean isSettable(EmailField control) {
                return !control.showValidationIcon.isBound();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables, SHOW_MAIL_ICON, SHOW_VALIDATION_ICON);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    // Custom email validation method
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
