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

    };

    function configureCreateSpaceButton() {

        var dataForDialog = {
            url1: 'url1-text'
        }

        function onCloseCallback(result) {
            console.log("onCloseCallback : ", result);
        }

        //https://developer.atlassian.com/cloud/jira/software/jsapi/classes/dialogoptions/
        //https://developer.atlassian.com/cloud/jira/software/modules/dialog/
        AJS.$("#create-space-configuration").click(function () {
            AP.dialog.create({
                key: 'space-dialog-key',
                width: '640px',
                height: '340px',
                chrome: true,
                customData: dataForDialog,
                header: 'Create space configuration',
                submitText: 'Save',
                buttons: [
                    {
                        text: 'Test connection',
                        identifier: 'test_connection'
                    }
                ]
            }).on("close", onCloseCallback);
        });
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

                var editButton = $('<aui-item-link >Edit</aui-item-link>').click(function (e) {
                    octanePluginContext.currentRow = rowInstance;
                    showWorkspaceConfigDialog();
                });
                var deleteButton = $('<aui-item-link >Remove</aui-item-link>').click(function (e) {
                    removeRow(rowInstance);
                });

                //add action button
                var dropdownId = "split-container-dropdown" + rowInstance.model.id;
                var topLevelEl = $('<div class="aui-buttons">' +
                    '<button class="aui-button aui-dropdown2-trigger aui-button-split-more aui-button-subtle aui-button-compact aui-dropdown2-trigger-arrowless" aria-controls="' + dropdownId + '">...</button></div>');
                var bottomLevelEl = $('<aui-dropdown-menu id="' + dropdownId + '"></aui-dropdown-menu>').append(editButton, deleteButton);
                var parentEl = $('<div></div>').append(topLevelEl, bottomLevelEl);
                return parentEl;
            }
        });

        spaceTable = new AJS.RestfulTable({
            el: jQuery("#space-table"),
            resources: {
                all: loadSpaces,
                //self: "/resources/configuration/workspaces/self"
            },
            columns: [
                {id: "label", header: "Label"},
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

    function removeRow(row){
        /*$("#workspace-to-delete").text(row.model.attributes.workspaceName);//update workspace name in dialog text

        AJS.dialog2("#warning-dialog").show();
        AJS.$("#warning-dialog-confirm").click(function (e) {
            e.preventDefault();
            AJS.dialog2("#warning-dialog").hide();

            $.ajax({url: octanePluginContext.configRestTable.options.resources.self +"/" + row.model.id, type: "DELETE",
            }).done(function () {
                octanePluginContext.configRestTable.removeRow(row);
            });
        });

        AJS.$("#warning-dialog-cancel").click(function (e) {
            e.preventDefault();
            AJS.dialog2("#warning-dialog").hide();
        });*/
    }

























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
                        callback(result)
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
};


