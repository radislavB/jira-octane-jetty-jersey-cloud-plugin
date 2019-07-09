var validateRequiredFieldsFilledAuto = false;
var customData;
var suggestedUdf;
AP.dialog.disableCloseOnSubmit();

AP.dialog.getCustomData(function (data) {
    console.log("getCustomData", data);
    customData = data;
    initDialog();
});

function initDialog() {
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

    if (isEditMode()) {
        console.log("edit mode ", customData.entity);
        $('#spaceSelector').val([customData.entity.spaceConfiguration.id]);
        $('#workspaceSelector').val([customData.entity.workspace.id]);
        $('#octaneUdf').val([customData.entity.octaneUdf]);
        $('#octaneEntityTypes').val([customData.entity.octaneEntityTypesLabels]);
        $('#jiraIssueTypesSelector').val(_.pluck(customData.entity.jiraIssueTypes, "id"));
        $('#jiraProjectsSelector').val(_.pluck(customData.entity.jiraProjects, "id"));

        loadPossibleOctaneUdf(customData.entity.workspace.id, customData.entity.spaceConfiguration.id);
        loadWorkspaces(customData.entity.spaceConfiguration.id, customData.entity.id);
    }

    setComboNoData("#workspaceSelector");
    setComboNoData("#jiraProjectsSelector", true);
    setComboNoData("#jiraIssueTypesSelector", true);
    setComboData("#spaceSelector", false, customData.spaces);

    $("#spaceSelector").change(function () {
        var spaceId = $("#spaceSelector").select2('data').id;
        loadWorkspaces(spaceId, isEditMode() ? customData.entity.id : null);
    });

    $("#workspaceSelector").change(function () {
        var workspaceId = $("#workspaceSelector").val();
        var space = $("#spaceSelector").select2('data');
        loadPossibleOctaneUdf(workspaceId, space.id);
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

    loadJiraProjects();
    loadJiraIssueTypes();
}

function loadPossibleOctaneUdf(workspaceId, spaceConfigurationId) {
    suggestedUdf = null;
    console.log("loadPossibleOctaneUdf", workspaceId, spaceConfigurationId);
    if (workspaceId && spaceConfigurationId) {
        var url = "/rest/octane/possible-jira-fields?space-configuration-id=" + spaceConfigurationId + "&workspace-id=" + workspaceId;
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
    return suggestedUdf;
}

function loadWorkspaces(spaceId, workspaceConfigurationId) {

    setComboNoData("#workspaceSelector");
    showLoadingIcon("#workspaceSelector");

    var url = "/rest/octane/workspaces?space-configuration-id=" + spaceId + "&workspace-configuration-id=" + workspaceConfigurationId;

    hostAjaxGet(url).then(function (result) {
        setComboData("#workspaceSelector", false, result);
    }).catch(function (error) {
        showFlag("Failed to fetch workspaces : " + error.message, "error");
    }).finally(function () {
        hideLoadingIcon("#workspaceSelector");
    });
}

function loadJiraProjects() {
    loadDataFromJiraToCombo("#jiraProjectsSelector", '/rest/api/latest/project', customData.usedJiraProjectIds);
}

function loadJiraIssueTypes() {
    loadDataFromJiraToCombo("#jiraIssueTypesSelector", '/rest/api/3/issuetype');
}

function loadDataFromJiraToCombo(selector, jiraRequestUrl, excludeIds) {
    showLoadingIcon(selector);
    AP.require('request', function (request) {
        request({
            url: jiraRequestUrl,
            success: function (response) {
                var arr = JSON.parse(response);
                var arr2Combo = _.map(arr, function (item) {
                    return {id: item.id, text: item.name};
                });
                if (excludeIds) {
                    arr2Combo = _.filter(arr2Combo, function (item) {
                        return !excludeIds.includes(item.id);
                    });
                }

                setComboData(selector, true, arr2Combo);
                hideLoadingIcon(selector);
            },
            error: function (e) {
                console.log(e);
                hideLoadingIcon(selector);
            }
        });
    });
}


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
            console.log("submitted entity", result);
            AP.dialog.close({entity: result});
        }).catch(function (error) {
        showFlag("Failed to save : " + error.message, "error");
    });
}