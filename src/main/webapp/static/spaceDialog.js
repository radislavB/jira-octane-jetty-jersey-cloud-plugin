var validateRequiredFieldsFilledAuto = false;
AP.dialog.disableCloseOnSubmit();
AP.dialog.getCustomData(function (customData) {
    console.log("received custom data2", customData);
});
AP.events.on('dialog.button.click', function (data) {
    console.log("SpaceDialog dialog.button.click", data.button);
    if (data.button.name === 'submit') {
        saveSpaceConfig();
    } else if (data.button.name === 'test_connection') {
        testConnection();
    }
});

function getPropertiesAsJson() {
    var data = {
        label: $("#label").attr("value"),
        location: $("#location").attr("value"),
        clientId: $("#clientId").attr("value"),
        clientSecret: $("#clientSecret").attr("value")
    };
    return JSON.stringify(data);
}


function validateRequiredFieldsFilled() {
    if (!validateRequiredFieldsFilledAuto) {
        validateRequiredFieldsFilledAuto = true;
        $(".required").change(function () {
            validateRequiredFieldsFilled();
        });
    }

    var validationFailed = !validateMissingRequiredAndUpdateErrorField($("#location").val(), "#locationError");
    validationFailed = !validateMissingRequiredAndUpdateErrorField($("#label").val(), "#labelError") || validationFailed;
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
    hostAjaxPost("/rest/configuration/test", getPropertiesAsJson())
        .then(function () {
            setStatus("Test connection is successful", "success");
        }).catch(function (error) {
        setStatus("Test connection is failed : " + error.message, "failed");
    });
}

function saveSpaceConfig() {
    if (!validateRequiredFieldsFilled()) {
        return;
    }
    setStatus("Saving ...");
    hostAjaxPost("/rest/configuration/spaces", getPropertiesAsJson())
        .then(function (result) {
            setStatus("Saved successfully", "success");

            var flag = AP.flag.create({
                //title: 'Saving',
                close: 'auto',
                body: 'Space configuration saved successfully.',
                type: 'success'
            });

            AP.dialog.close({entity: result});
        }).catch(function (error) {
        setStatus("Failed to save : " + error.message, "failed");
    });
}