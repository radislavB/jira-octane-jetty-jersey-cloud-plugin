function activateOctaneConfigPage() {

    var workspacesRestfulTable;
    function initConfigurationPage() {
        //initWorkspaceTables();
        //loadSpaceConfiguration();
        configureCreateSpaceButton();
        //configureSpaceButtons();
        configureWorkspaceButtons();

    };

    function initWorkspaceTables() {

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
                var instance = this;


                var editButton = $('<aui-item-link >Edit</aui-item-link>').click(function (e) {
                    octanePluginContext.currentRow = instance;
                    showWorkspaceConfigDialog();
                });
                var deleteButton = $('<aui-item-link >Remove</aui-item-link>').click(function (e) {
                    removeRow(instance);
                });

                //add action button
                var dropdownId = "split-container-dropdown" + instance.model.id;
                var topLevelEl = $('<div class="aui-buttons">' +
                    '<button class="aui-button aui-dropdown2-trigger aui-button-split-more aui-button-subtle aui-button-compact" aria-controls="' + dropdownId + '">...</button></div>');
                var bottomLevelEl = $('<aui-dropdown-menu id="' + dropdownId + '"></aui-dropdown-menu>').append(editButton, deleteButton);
                var parentEl = $('<div></div>').append(topLevelEl, bottomLevelEl);
                return parentEl;
            }
        });

        workspacesRestfulTable = new AJS.RestfulTable({
            el: jQuery("#configuration-resources-table"),
            resources: {
                all: loadWorkspaces,//"/resources/configuration/workspaces",
                self: "/resources/configuration/workspaces/self"
            },
            columns: [
                {id: "id", header: "Workspace Id"},
                {id: "workspaceName", header: "Workspace Name"},
                //{id: "octaneUdf", header: "Mapping Field"},
                //{id: "octaneEntityTypes", header: "Entity Types", readView: ListReadView},
                //{id: "jiraIssueTypes", header: "Jira Issue Types", readView: ListReadView},
                //{id: "jiraProjects", header: "Jira Project", readView: ListReadView}
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

    function reloadTable(table){
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
                    if(callback && $.isFunction(callback)){
                        callback(result)
                    }

                },
                error: function(xhr) { // if error occurred
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
                error: function(xhr) { // if error occurred
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

    function configureCreateSpaceButton(){

        var customData={
            url1: 'url1-text'
        }

        function onCloseCallback(result){
            console.log("onCloseCallback : ", result);
        }

        //https://developer.atlassian.com/cloud/jira/software/jsapi/classes/dialogoptions/
        //https://developer.atlassian.com/cloud/jira/software/modules/dialog/
        AJS.$("#create-space-configuration").click(function () {
            AP.dialog.create({
                key: 'space-dialog-key',
                width: '640px',
                height: '300px',
                chrome: true,
                customData: customData,
                header:'Create space configuration',
                submitText:'Save',
                buttons: [
                    {
                        text: 'Test connection',
                        identifier: 'test_connection'
                    }
                ]
            }).on("close", onCloseCallback);
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


