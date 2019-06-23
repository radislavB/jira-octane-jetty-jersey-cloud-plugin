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

    setComboData("#spaceSelector", false, customData.spaces);
    setComboNoData("#workspaceSelector");


    $("#spaceSelector").change(function () {
        setComboNoData("#workspaceSelector");
        setStatusLoading("#workspaceSelector");
        var space = $("#spaceSelector").select2('data');
        var url = "/rest/octane/workspaces?space-configuration-id=" + space.id;

        hostAjaxGet(url).then(function (result) {
            setComboData("#workspaceSelector", false, result);
        }).catch(function (error) {
            showFlag("Failed to fetch workspace from space '" + space.text + " : " + error.message, "error");
        }).finally(function () {
            removeStatusLoading("#workspaceSelector");
        });
    });

    $("#workspaceSelector").change(function () {

        var workspaceId = $("#workspaceSelector").val();
        if (workspaceId) {
            var space = $("#spaceSelector").select2('data');
            var url = "/rest/octane/possible-jira-fields?space-configuration-id=" + space.id + "&workspace-id=" + workspaceId;
            setStatusLoading("#octaneUdf");
            hostAjaxGet(url).then(function (result) {
                console.log("possible-jira-fields : " + result);
                setStatusInfo("#octaneUdf","possible-jira-fields : " + result);
            }).catch(function (error) {
                console.log("failed to fetch possible-jira-fields  : " + error.message);
                setStatusInfo("#octaneUdf","failed to fetch possible-jira-fields  : " + error.message);
            });
        }

    });

});

function setComboNoData(selector) {
    AJS.$(selector).auiSelect2({
        multiple: false,
        data: []
    });
    $(selector).prop('disabled', true); //disable selector
}

function setComboData(selector, multiple, data) {
    AJS.$(selector).auiSelect2({
        multiple: multiple,
        data: data
    });
    $(selector).prop('disabled', false); //disable selector
}

function setStatusLoading(selector, title) {
    setStatus(selector, "statusLoading", title);
}

function setStatusInfo(selector, title) {
    setStatus(selector, "statusInfo", title);
}

function setStatus(selector, statusClass, title) {
    var statusEl = $(selector + "+.status");
    statusEl.removeClass("statusInfo statusLoading");
    statusEl.addClass(statusClass);
    if (title) {
        statusEl.attr("title", title);
    } else {
        statusEl.attr("title", "");
    }
}

function removeStatusLoading(selector) {
    $(selector + "+.status").removeClass("statusLoading");
    $(selector + "+.status").attr("title", "");
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