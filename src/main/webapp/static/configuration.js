function initConfigurationPage() {
    loadSpaceConfiguration();
    //configureSpaceButtons();
}

function loadSpaceConfiguration() {

    AP.context.getToken(function (token) {
        $.ajax({
            url: "/rest/configuration",
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

function configureSpaceButtons() {
    console.log("configureSpaceButtons");
    console.log(AJS.$("#save-space-configuration"));
    AJS.$("#save-space-configuration").click(function () {
        updateSpaceConfig();
    });

    function updateSpaceConfig() {
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
    }
}
