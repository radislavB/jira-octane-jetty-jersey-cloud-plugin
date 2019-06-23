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

    //set fields
    if (isEditMode()) {
        console.log("edit mode");
        //$("#name").val(customData.entity.name);
        //$("#location").val(customData.entity.location);
        //$("#clientId").val(customData.entity.clientId);
        //$("#clientSecret").val(customData.entity.clientSecret);
    }

    var noData = [];

    AJS.$("#spaceSelector").auiSelect2({
        multiple: false,
        data: customData.spaces
    });


    initSelect2("#workspaceSelector");


    $("#spaceSelector").change(function () {
        initSelect2("#workspaceSelector");//clear workspaces

        var space = $("#spaceSelector").select2('data');
        var url = "/rest/configuration/spaces/" + space.id + "/data/workspaces";
        hostAjaxGet(url).then(function (result) {
            AJS.$("#workspaceSelector").auiSelect2({
                multiple: false,
                data: result
            });
        }).catch(function (error) {
            showFlag("Failed to fetch workspace from space '" + space.text + " : " + error.message, "error");
        });
    });

});

function initSelect2(selector, multiple){
    AJS.$(selector).auiSelect2({
        multiple: multiple,
        data: []
    });
}
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
            showFlag('Workspace configuration saved successfully.');
            AP.dialog.close({entity: result});
        }).catch(function (error) {
        setStatus("Failed to save : " + error.message, "failed");
    });
}