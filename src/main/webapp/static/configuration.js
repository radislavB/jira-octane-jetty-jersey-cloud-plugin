function activateOctaneConfigPage() {

    var spaceTable;
    var workspacesRestfulTable;

    function initConfigurationPage() {
        configureCreateSpaceButton();
        initSpaceTables();

        //initWorkspaceTables();
        //loadSpaceConfiguration();

        //configureSpaceButtons();
        //configureWorkspaceButtons();

    }

    function configureCreateSpaceButton() {
        AJS.$("#create-space-configuration").click(function () {
            showSpaceConfigurationDialog();
        });
    }

    function showSpaceConfigurationDialog(rowForEdit) {

        var editMode = !!rowForEdit;
        var editEntity = editMode ? rowForEdit.model.attributes : null;

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
            width: '640px',
            height: '340px',
            chrome: true,
            customData: {editMode: editMode, entity: editEntity},
            header: 'Create space configuration',
            submitText: 'Save',
            buttons: [
                {
                    text: 'Test connection',
                    identifier: 'test_connection'
                }
            ]
        }).on("close", onCloseCallback);
    }


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


    function loadSpaces(callback) {
        console.log("loadSpaces");
        hostAjaxGet("/rest/configuration/spaces")
            .then(function (data) {
                callback(data);
            });
    }

    function initSpaceTables() {

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
                    removeSpace(spaceTable, rowInstance);
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

    function removeSpace(table, row) {
        console.log(row);
        var spaceName = row.model.attributes.name;
        var text = "Are you sure you want to delete space configuration '" + spaceName + "' ?";
        confirmDelete(text).then(function (isConfirmed) {
            if (isConfirmed) {
                hostAjaxDelete(table.options.resources.self + row.model.id)
                    .then(function (data) {
                        table.removeRow(row);
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
        var firstChild = statusEl.children().first();
        if (!firstChild.hasClass("aui-icon")) {
            firstChild.remove();
            statusEl.append("<span class=\"aui-icon aui-icon-small space-save-status\"></span>");
            firstChild = statusEl.children().first();
        }
        console.log("testConnection", statusEl);
        console.log("firstChild", firstChild);

        hostAjaxPost("/rest/configuration/spaces/test-connection", JSON.stringify(row.model.attributes))
            .then(function () {
                console.log("test connection is successful");
                firstChild.addClass("aui-iconfont-successful-build");
            }).catch(function (error) {
            console.log("test connection failed ", error.message);
        });
    }

    function setRowStatus() {

    }


    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    function reloadTable(table) {
        console.log("reloadTable");
        table.$tbody.empty();
        table.fetchInitialResources();
    }

    function loadWorkspaces(callback) {

        AP.context.getToken(function (token) {
            $.ajax({
                url: "/resources/configuration/workspaces",
                type: "GET",
                dataType: "json",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Authorization", "JWT " + token);
                },
                success: function (result) {
                    console.log("loadWorkspaces success : " + result);
                    if (callback && $.isFunction(callback)) {
                        callback(result);
                    }

                },
                error: function (xhr) { // if error occurred
                    console.log("error : " + xhr);
                }
            });
        });
    }

    function loadSpaceConfiguration() {

        AP.context.getToken(function (token) {
            $.ajax({
                url: "/resources/configuration",
                type: "GET",
                //data: { testdata: "1234" },
                dataType: "json",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Authorization", "JWT " + token);
                },
                success: function (result) {
                    console.log("success : " + result);
                },
                error: function (xhr) { // if error occurred
                    console.log("error : " + xhr);
                }
            });
        });
    }

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


