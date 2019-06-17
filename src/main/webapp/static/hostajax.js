function hostAjaxGet(url, success, error) {
    hostAjaxSend('GET', url, "", success, error);
}

function hostAjaxPost(url, data, success, error) {
    console.log('post');
    hostAjaxSend('POST', url, data, success, error);
}

function hostAjaxSend(type1, url, data, successCallback, errorCallback) {

    console.log(type1, ":", url);
    AP.context.getToken(function (token) {
        console.log("token received");
        $.ajax({
            url: url,
            type: type1,
            data: data,
            dataType: "json",
            contentType: "application/json",
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "JWT " + token);
            },
            success: function (result) {
                console.log("success", type1, ":", url, " - ", result);
                if (successCallback && $.isFunction(successCallback)) {
                    successCallback(result)
                }
            },
            error: function (xhr) { // if error occurred
                console.log("error", type1, ":", url, " - ", xhr);
                if (errorCallback && $.isFunction(errorCallback)) {
                    errorCallback(xhr)
                }
            }
        });
    });
}