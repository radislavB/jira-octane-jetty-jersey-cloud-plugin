var validateRequiredFieldsFilledAuto = false;
var customData;
AP.dialog.disableCloseOnSubmit();
AP.dialog.getCustomData(function (data) {
    customData = data;
    //set header
    $(".aui-dialog2-header-main").text(customData.header);

    //assign buttons
    $("#confirm-button").click(function (e) {
        e.preventDefault();
        submit();
    });
    $("#cancel-button").click(function (e) {
        e.preventDefault();
        AP.dialog.close();
    });

    $("#test-connection-button").click(function (e) {
        e.preventDefault();
        testConnection();
    });

    //set fields
    if (isEditMode()) {
        $("#name").val(customData.entity.name);
        $("#location").val(customData.entity.location);
        $("#clientId").val(customData.entity.clientId);
        $("#clientSecret").val(customData.entity.clientSecret);
    }
});

function getProperties() {
    var data = {
        name: $("#name").attr("value"),
        location: $("#location").attr("value"),
        clientId: $("#clientId").attr("value"),
        clientSecret: $("#clientSecret").attr("value")
    };
    if (isEditMode()) {
        data.id = customData.entity.id;
    }
    return data;
}

function isEditMode() {
    return customData.editMode && customData.entity;
}

function validateRequiredFieldsFilled() {
    if (!validateRequiredFieldsFilledAuto) {
        validateRequiredFieldsFilledAuto = true;
        $(".required").change(function () {
            validateRequiredFieldsFilled();
        });
    }

    var validationFailed = !validateMissingRequiredAndUpdateErrorField($("#location").val(), "#locationError");
    validationFailed = !validateMissingRequiredAndUpdateErrorField($("#name").val(), "#nameError") || validationFailed;
    validationFailed = !validateMissingRequiredAndUpdateErrorField($("#clientId").val(), "#clientIdError") || validationFailed;
    validationFailed = !validateMissingRequiredAndUpdateErrorField($("#clientSecret").val(), "#clientSecretError") || validationFailed;
    return !validationFailed;
}

function setStatus(text, status) {
    $("#statusDialog").text(text);
    $("#statusDialog").removeClass("statusSuccess").removeClass("statusFailed");
    if (status) {
        if (status === 'success') {
            $("#statusDialog").addClass("statusSuccess");
        } else if (status === 'failed') {
            $("#statusDialog").addClass("statusFailed");
        }
    }
}

function testConnection() {
    if (!validateRequiredFieldsFilled()) {
        return;
    }
    setStatus("Test connection ...");
    hostAjaxPost("/rest/configuration/spaces/test-connection", JSON.stringify(getProperties()))
        .then(function () {
            setStatus("Test connection is successful", "success");
        }).catch(function (error) {
        setStatus("Test connection is failed : " + error.message, "failed");
    });
}

function submit() {
    if (!validateRequiredFieldsFilled()) {
        return;
    }
    setStatus("Saving ...");
    var entityProperties = getProperties();
    var requestType;
    var url;
    if (isEditMode()) {
        requestType = "PUT";
        url = "/rest/configuration/spaces/" + entityProperties.id;
    } else {
        requestType = "POST";
        url = "/rest/configuration/spaces";
    }

    hostAjaxSend(requestType, url, JSON.stringify(entityProperties))
        .then(function (result) {
            showFlag('Space configuration saved successfully.');
            AP.dialog.close({entity: result});
        }).catch(function (error) {
        setStatus("Failed to save : " + error.message, "failed");
    });
}