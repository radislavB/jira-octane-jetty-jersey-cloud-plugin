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

    var suggestedUdf;
    var noData = [];

    setComboData("#spaceSelector", false, customData.spaces);
    setComboNoData("#workspaceSelector");
    setComboNoData("#jiraProjectsSelector", true);
    setComboNoData("#jiraIssueTypesSelector", true);


    $("#spaceSelector").change(function () {
        setComboNoData("#workspaceSelector");
        showLoadingIcon("#workspaceSelector");
        var space = $("#spaceSelector").select2('data');
        var url = "/rest/octane/workspaces?space-configuration-id=" + space.id;

        hostAjaxGet(url).then(function (result) {
            setComboData("#workspaceSelector", false, result);
        }).catch(function (error) {
            showFlag("Failed to fetch workspace from space '" + space.text + " : " + error.message, "error");
        }).finally(function () {
            hideLoadingIcon("#workspaceSelector");
        });
    });

    $("#workspaceSelector").change(function () {

        suggestedUdf = null;
        var workspaceId = $("#workspaceSelector").val();
        if (workspaceId) {
            var space = $("#spaceSelector").select2('data');
            var url = "/rest/octane/possible-jira-fields?space-configuration-id=" + space.id + "&workspace-id=" + workspaceId;
            showLoadingIcon("#octaneUdf");
            setTitle("#octaneUdfInfo", "Searching for suggested fields ...");
            hostAjaxGet(url).then(function (data) {
                if (data && data.length) {
                    var msg = "Suggested ALM Octane fields: " + data.join(",  ") + ". Double-click to set '" + data[0] + "' as value.";
                    suggestedUdf = data[0];
                    setTitle("#octaneUdfInfo", msg, true);
                } else {
                    setTitle("#octaneUdfInfo", "No suggested fields are found.");
                }
            }).catch(function (error) {
                setTitle("#octaneUdfInfo", "Failed to fetch possible-jira-fields  : " + error.message);
                console.log("Failed to fetch possible-jira-fields  : " + error.message);
            }).finally(function () {
                hideLoadingIcon("#octaneUdf");
            });
        }
    });

    $("#octaneUdfInfo").dblclick(function (e) {
        if (suggestedUdf) {
            $("#octaneUdf").val(suggestedUdf);
            refreshOctaneEntityTypes();
        }
    });

    $(".affect-octane-entity-types").change(function () {
        refreshOctaneEntityTypes();
    });

    $("#refreshOctaneEntityTypesButton").click(function (e) {
        e.preventDefault();
        refreshOctaneEntityTypes();
    });

    console.log("Get jira projects");
    showLoadingIcon("#jiraProjectsSelector");
    AP.require('request', function (request) {
        request({
            url: '/rest/api/latest/project',
            success: function (response) {
                var projectArr = JSON.parse(response);
                var projects2Combo = _.map(projectArr, function (item) {
                    return {id: item.id, text: item.name};
                });
                setComboData("#jiraProjectsSelector", true, projects2Combo);
                hideLoadingIcon("#jiraProjectsSelector");
            },
            error: function (e) {
                console.log(e);
                hideLoadingIcon("#jiraProjectsSelector");
            }
        });
    });

    console.log("Get jira types");
    showLoadingIcon("#jiraIssueTypesSelector");
    AP.require('request', function (request) {
        request({
            url: '/rest/api/3/issuetype',
            success: function (response) {
                var typesArr = JSON.parse(response);
                var type2Combo = _.map(typesArr, function (item) {
                    return {id: item.id, text: item.name};
                });
                setComboData("#jiraIssueTypesSelector", true, type2Combo);
                hideLoadingIcon("#jiraIssueTypesSelector");
            },
            error: function (e) {
                console.log(e);
                hideLoadingIcon("#jiraIssueTypesSelector");
            }
        });
    });

});

function setComboNoData(selector, multiple) {
    AJS.$(selector).auiSelect2({
        multiple: !!multiple,
        data: []
    });
    $(selector).prop('disabled', true); //disable selector
}

function setComboData(selector, multiple, data) {
    var mydata = _.sortBy(data, 'text');
    AJS.$(selector).auiSelect2({
        multiple: !!multiple,
        data: mydata
    });
    $(selector).prop('disabled', false); //enable selector
}

function showLoadingIcon(selector) {
    $(selector + "+.loading").addClass("loadingActive");
}

function hideLoadingIcon(selector) {
    $(selector + "+.loading").removeClass("loadingActive");
}

function setTitle(selector, title, filled) {
    $(selector).attr("title", title);
    $(selector).toggleClass("infoFilled", !!filled);
}

function refreshOctaneEntityTypes() {
    var space = $("#spaceSelector").select2('data');
    var workspace = $("#workspaceSelector").select2('data');
    var octaneUdf = $("#octaneUdf").attr("value");
    if (space && workspace && octaneUdf) {
        var url = "/rest/octane/supported-types?space-configuration-id=" + space.id + "&workspace-id=" + workspace.id + "&udf-name=" + octaneUdf;
        showLoadingIcon("#octaneEntityTypes");
        hostAjaxGet(url).then(function (data) {
            $("#octaneEntityTypes").val(data);
        }).catch(function (error) {
            console.log("Failed to fetch supported octane types  : " + error.message);
        }).finally(function () {
            hideLoadingIcon("#octaneEntityTypes");
        });
    }
}

function getProperties() {
    var data = {
        spaceConfiguration: $("#spaceSelector").select2('data'),
        workspace: $("#workspaceSelector").select2('data'),
        octaneUdf: $("#octaneUdf").attr("value"),
        octaneEntityTypesLabels: ($("#octaneEntityTypes").val()) ? $("#octaneEntityTypes").attr("value").split(",") : [], //if empty value - send empty array
        jiraIssueTypes: $("#jiraIssueTypesSelector").select2('data'),
        jiraProjects: $("#jiraProjectsSelector").select2('data')
    };

    if (isEditMode()) {
        data.id = customData.entity.id;
    }

    console.log("getProperties", data);
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

    var validationFailed = !validateMissingRequiredAndUpdateErrorField($("#spaceSelector").select2('data'), "#spaceSelectorError");
    validationFailed = !validateMissingRequiredAndUpdateErrorField($("#workspaceSelector").select2('data'), "#workspaceSelectorError") || validationFailed;
    validationFailed = !validateMissingRequiredAndUpdateErrorField($("#octaneUdf").attr("value"), "#octaneUdfError") || validationFailed;
    validationFailed = !validateMissingRequiredAndUpdateErrorField($("#octaneEntityTypes").val(), "#octaneEntityTypesError") || validationFailed;
    validationFailed = !validateMissingRequiredAndUpdateErrorField($("#jiraProjectsSelector").select2('data').length, "#jiraProjectsSelectorError") || validationFailed;
    validationFailed = !validateMissingRequiredAndUpdateErrorField($("#jiraIssueTypesSelector").select2('data').length, "#jiraIssueTypesSelectorError") || validationFailed;

    return !validationFailed;
}


function submit() {
    if (!validateRequiredFieldsFilled()) {
        return;
    }

    var entityProperties = getProperties();
    var requestType;
    var url;
    if (isEditMode()) {
        requestType = "PUT";
        url = "/rest/configuration/workspaces/" + entityProperties.id;
    } else {
        requestType = "POST";
        url = "/rest/configuration/workspaces";
    }

    hostAjaxSend(requestType, url, JSON.stringify(entityProperties))
        .then(function (result) {
            showFlag('Workspace configuration saved successfully.');
            AP.dialog.close({entity: result});
        }).catch(function (error) {
        showFlag("Failed to save : " + error.message, "error");
    });
}