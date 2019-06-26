function activateOctaneConfigPage() {

    var spaceTable;
    var workspaceTable;
    var workspacesRestfulTable;

    function initConfigurationPage() {
        configureCreateSpaceButton();
        configureCreateWorkspaceButton();
        initSpaceTable();
        initWorkspaceTable();
    }

    function configureCreateSpaceButton() {
        AJS.$("#create-space-configuration").click(function () {
            showSpaceConfigurationDialog();
        });
    }

    function configureCreateWorkspaceButton() {
        AJS.$("#create-workspace-configuration").click(function () {
            showWorkspaceConfigurationDialog();
        });
    }

    function showSpaceConfigurationDialog(rowForEdit) {
        var editMode = !!rowForEdit;
        var editEntity = editMode ? rowForEdit.model.attributes : null;
        var header = editMode ? "Edit space configuration" : "Create space configuration";

        function onCloseCallback(result) {
            if (result && result.entity) {
                if (editMode) {
                    var rowModel = rowForEdit.model.attributes;
                    rowModel.location = result.entity.location;
                    rowModel.name = result.entity.name;
                    rowModel.clientId = result.entity.clientId;
                    rowModel.clientSecret = result.entity.clientSecret;
                    rowForEdit.render();
                } else {
                    spaceTable.addRow(result.entity);
                }

            }
        }

        AP.dialog.create({
            key: 'space-dialog-key',
            width: '660px',
            height: '480px',
            chrome: false,
            customData: {editMode: editMode, entity: editEntity, header: header},
        }).on("close", onCloseCallback);
    }

    function loadSpaceConfigurations(callback) {
        hostAjaxGet("/rest/configuration/spaces")
            .then(function (data) {
                callback(data);
            });
    }

    function initSpaceTable() {

        var MyRow = AJS.RestfulTable.Row.extend({
            renderOperations: function () {
                var rowInstance = this;

                var editButtonEl = $('<button class=\"aui-button aui-button-link\">Edit</button>').click(function (e) {
                    showSpaceConfigurationDialog(rowInstance);
                });

                var testConnectionButtonEl = $('<button class=\"aui-button aui-button-link\">Test Connection</button>').click(function (e) {
                    testConnection(rowInstance);
                });

                var deleteButtonEl = $('<button class=\"aui-button aui-button-link\">Delete</button>').click(function (e) {
                    removeSpaceConfiguration(rowInstance);
                });

                var parentEl = $('<div></div>').append(editButtonEl, deleteButtonEl, testConnectionButtonEl);
                return parentEl;
            }
        });

        spaceTable = new AJS.RestfulTable({
            el: jQuery("#space-table"),
            resources: {
                all: loadSpaceConfigurations,
                self: "/rest/configuration/spaces/"
            },
            columns: [
                {id: "name", header: "Name"},
                {id: "location", header: "Location"},
                {id: "clientId", header: "Client Id"}
            ],
            autoFocus: false,
            allowEdit: false,
            allowReorder: false,
            allowCreate: false,
            allowDelete: false,
            noEntriesMsg: "No space configuration is defined.",
            loadingMsg: "Loading ...",
            views: {
                row: MyRow
            }
        });
    }

    function removeSpaceConfiguration(row) {
        var spaceName = row.model.attributes.name;
        var text = "Are you sure you want to delete space configuration '" + spaceName + "' ?";
        confirmDelete(text).then(function (isConfirmed) {
            if (isConfirmed) {
                hostAjaxDelete(spaceTable.options.resources.self + row.model.id)
                    .then(function (data) {
                        spaceTable.removeRow(row);
                        showFlag("Space configuration '" + spaceName + "' was deleted successfully.");
                    }).catch(function (error) {
                    showFlag("Failed to delete space  configuration '" + spaceName + "' : " + error.message, "error");
                });
            }
        });
    }

    function confirmDelete(confirmationText) {
        return new Promise(function (resolve, reject) {
            function onCloseCallback(result) {
                var output = (result && result.confirmed) ? true : false;
                resolve(output);
            }

            AP.dialog.create({
                key: 'confirmation-dialog-key',
                width: '550px',
                height: '300px',
                chrome: false,
                customData: {confirmationText: confirmationText}
            }).on("close", onCloseCallback);
        });
    }

    function testConnection(row) {

        var statusEl = row.$el.children().eq(4);
        var throbber = statusEl.children().first();
        throbber.addClass("test-connection-status");
        throbber.removeClass("test-connection-status-successful");
        throbber.removeClass("test-connection-status-failed");
        throbber.attr("title", "Testing connection ...");

        hostAjaxPost("/rest/configuration/spaces/test-connection", JSON.stringify(row.model.attributes))
            .then(function () {
                throbber.addClass("test-connection-status-successful");
                throbber.attr("title", "Test connection is successful");
            }).catch(function (error) {
            throbber.addClass("test-connection-status-failed");
            throbber.attr("title", "Test connection is failed : " + error.message);
        });
    }

    function initWorkspaceTable() {

        //view that show list items in stacked format
        var ListReadView = AJS.RestfulTable.CustomReadView.extend({
            render: function (self) {
                var output = _.reduce(self.value, function (memo, current) {
                    return memo + '<li>' + current + '</li>';
                }, '<ul class="simple-list">');
                output += '</ul>';
                return output;
            }
        });

        var MyRow = AJS.RestfulTable.Row.extend({
            renderOperations: function () {
                var rowInstance = this;

                var editButtonEl = $('<button class=\"aui-button aui-button-link\">Edit</button>').click(function (e) {
                    showWorkspaceConfigurationDialog(rowInstance);
                });

                var deleteButtonEl = $('<button class=\"aui-button aui-button-link\">Delete</button>').click(function (e) {
                    console.log("deleteButtonEl clicked");
                    removeWorkspaceConfiguration(rowInstance);
                });

                var parentEl = $('<div></div>').append(editButtonEl, deleteButtonEl);
                return parentEl;
            }
        });

        workspaceTable = new AJS.RestfulTable({
            el: jQuery("#workspace-table"),
            resources: {
                all: loadWorkspaceConfigurations,
                self: "/rest/configuration/workspaces/"
            },
            columns: [
                {id: "spaceName", header: "Space"},
                {id: "workspaceId", header: "Workspace Id"},
                {id: "workspaceName", header: "Workspace Name"},
                {id: "octaneUdf", header: "Mapping Field"},
                {id: "octaneEntityTypes", header: "Entity Types", readView: ListReadView},
                {id: "jiraIssueTypes", header: "Jira Issue Types", readView: ListReadView},
                {id: "jiraProjects", header: "Jira Projects", readView: ListReadView}
            ],
            autoFocus: false,
            allowEdit: false,
            allowReorder: false,
            allowCreate: false,
            allowDelete: false,
            noEntriesMsg: "No workspace configuration is defined.",
            loadingMsg: "Loading ...",
            views: {
                row: MyRow
            }
        });
    }

    function showWorkspaceConfigurationDialog(rowForEdit) {
        var spaces = _.map(spaceTable.getModels().models, function (item) {
            return {id: item.attributes.id, text: item.attributes.name};
        });

        var editMode = !!rowForEdit;
        var editEntity = editMode ? rowForEdit.model.attributes : null;
        var header = editMode ? "Edit workspace configuration" : "Create workspace configuration";

        function onCloseCallback(result) {
            if (result && result.entity) {
                console.log(result.entity);
                if (editMode) {
                    //var rowModel = rowForEdit.model.attributes;
                    //rowModel.location = result.entity.location;
                    //rowModel.name = result.entity.name;
                    //rowModel.clientId = result.entity.clientId;
                    //rowModel.clientSecret = result.entity.clientSecret;
                    //rowForEdit.render();
                } else {
                    workspaceTable.addRow(convertServerWorkspaceConfigurationToTableEntity(result.entity));
                }
            }
        }

        AP.dialog.create({
            key: 'workspace-dialog-key',
            width: '660px',
            height: '550px',
            chrome: false,
            customData: {editMode: editMode, entity: editEntity, header: header, spaces: spaces},
        }).on("close", onCloseCallback);
    }

    function removeWorkspaceConfiguration(row) {
        var workspaceText = row.model.attributes.spaceName + "(" + row.model.attributes.workspaceId + ")"
        var text = "Are you sure you want to delete workspace configuration '" + workspaceText + "' ?";
        confirmDelete(text).then(function (isConfirmed) {
            if (isConfirmed) {
                hostAjaxDelete(workspaceTable.options.resources.self + row.model.id)
                    .then(function (data) {
                        workspaceTable.removeRow(row);
                        showFlag("Workspace configuration '" + workspaceText + "' was deleted successfully.");
                    }).catch(function (error) {
                    showFlag("Failed to delete workspace configuration '" + workspaceText + "' : " + error.message, "error");
                });
            }
        });
    }

    function loadWorkspaceConfigurations(callback) {
        hostAjaxGet("/rest/configuration/workspaces")
            .then(function (data) {
                var workspaces = _.map(data, function (item) {
                    return convertServerWorkspaceConfigurationToTableEntity(item);
                });

                callback(workspaces);
            });
    }

    function convertServerWorkspaceConfigurationToTableEntity(item) {
        return {
            id: item.id,
            spaceName: item.spaceConfiguration.text,
            workspaceId: item.workspace.id,
            workspaceName: item.workspace.text,
            octaneUdf: item.octaneUdf,
            octaneEntityTypes: item.octaneEntityTypesLabels,
            jiraIssueTypes: _.map(item.jiraIssueTypes, function (t) {
                return t.text;
            }),
            jiraProjects: _.map(item.jiraProjects, function (t) {
                return t.text;
            }),
            original: item
        };
    }

    function reloadTable(table) {
        console.log("reloadTable");
        table.$tbody.empty();
        table.fetchInitialResources();
    }

    initConfigurationPage();
}


