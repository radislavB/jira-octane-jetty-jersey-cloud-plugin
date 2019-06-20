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
            width: '650px',
            height: '540px',
            chrome: false,
            customData: {editMode: editMode, entity: editEntity, header: header},
        }).on("close", onCloseCallback);
    }

    function loadSpaces(callback) {
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
                all: loadSpaces,
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
        console.log(row);
        var spaceName = row.model.attributes.name;
        var text = "Are you sure you want to delete space configuration '" + spaceName + "' ?";
        confirmDelete(text).then(function (isConfirmed) {
            if (isConfirmed) {
                hostAjaxDelete(table.options.resources.self + row.model.id)
                    .then(function (data) {
                        spaceTable.removeRow(row);
                        showFlag("Space configuration '" + spaceName + "' was deleted successfully.");
                    }).catch(function (error) {
                    console.log(error);
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
                    removeWorkspaceConfiguration(rowInstance);
                });

                var parentEl = $('<div></div>').append(editButtonEl, deleteButtonEl);
                return parentEl;
            }
        });

        workspaceTable = new AJS.RestfulTable({
            el: jQuery("#workspace-table"),
            resources: {
                all: loadWorkspaces,
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
        var editMode = !!rowForEdit;
        var editEntity = editMode ? rowForEdit.model.attributes : null;
        var header = editMode ? "Edit workspace configuration" : "Create workspace configuration";

        function onCloseCallback(result) {
            if (result && result.entity) {
                console.log(result.entity);
                /*if (editMode) {
                    var rowModel = rowForEdit.model.attributes;
                    rowModel.location = result.entity.location;
                    rowModel.name = result.entity.name;
                    rowModel.clientId = result.entity.clientId;
                    rowModel.clientSecret = result.entity.clientSecret;
                    rowForEdit.render();
                } else {
                    spaceTable.addRow(result.entity);
                }*/

            }
        }

        AP.dialog.create({
            key: 'workspace-dialog-key',
            width: '650px',
            height: '540px',
            chrome: false,
            customData: {editMode: editMode, entity: editEntity, header: header},
        }).on("close", onCloseCallback);
    }

    function removeWorkspaceConfiguration(row) {

    }

    function loadWorkspaces(callback) {
        hostAjaxGet("/rest/configuration/workspaces")
            .then(function (data) {
                callback(data);
            });
    }

    function reloadTable(table) {
        console.log("reloadTable");
        table.$tbody.empty();
        table.fetchInitialResources();
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////



    function configureWorkspaceButtons() {
        AJS.$("#reload-workspaces").click(function () {
            reloadTable(workspacesRestfulTable);
        });
    }


    function configureSpaceButtons() {
        console.log("configureSpaceButtons");
        console.log(AJS.$("#save-space-configuration"));
        AJS.$("#save-space-configuration").click(function () {
            //updateSpaceConfig();
            //reloadTable();
        });

        /*function updateSpaceConfig() {
            console.log("updateSpaceConfig clicked");
            var data = {
                location: $("#location").attr("value"),
                clientId: $("#clientId").attr("value"),
                clientSecret: $("#clientSecret").attr("value"),
            };

            var dataJson = JSON.stringify(data);
            $('.space-save-status').removeClass("aui-iconfont-successful-build");
            $('.space-save-status').removeClass("aui-iconfont-error");

            $("#reloadSpinner").spin();
            enableSpaceSaveButton(false);

            $.ajax({
                url: octanePluginContext.octaneAdminBaseUrl,
                type: "PUT",
                data: dataJson,
                dataType: "json",
                contentType: "application/json"
            }).done(function (msg) {
                enableSpaceSaveButton(true);
                showSpaceStatus("Space configuration is saved successfully", true);
                $('.space-save-status').addClass("aui-iconfont-successful-build");
                $("#reloadSpinner").spinStop();

            }).fail(function (request, status, error) {
                console.log(status);
                enableSpaceSaveButton(true);
                var msg = request.responseText;
                if (!msg && status && status === 'timeout') {
                    msg = "Timeout : possibly proxy settings are missing.";
                }

                showSpaceStatus(msg, false);
                $('.space-save-status').addClass("aui-iconfont-error");
                $("#reloadSpinner").spinStop();
            });
        }*/
    }

    initConfigurationPage();
}


