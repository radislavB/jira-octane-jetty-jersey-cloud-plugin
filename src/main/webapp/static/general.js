function hostAjaxGet(url) {
    return hostAjaxSend('GET', url, "");
}

function hostAjaxDelete(url) {
    return hostAjaxSend('DELETE', url, "");
}

function hostAjaxPost(url, data) {
    return hostAjaxSend('POST', url, data);
}

function hostAjaxSend(requestType, url, data) {
    console.log(requestType, ":", url);
    return new Promise(function (resolve, reject) {
        AP.context.getToken(function (token) {
            $.ajax({
                url: url,
                type: requestType,
                data: data,
                dataType: "json",
                contentType: "application/json",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Authorization", "JWT " + token);
                },
                success: function (result) {
                    console.log("success", requestType, url, result);
                    resolve(result);
                },
                error: function (xhr) { // if error occurred
                    console.log("error", requestType, url, xhr.statusText, xhr.responseText);
                    var reason = new Error(xhr.responseText);
                    reject(reason);
                }
            });
        });
    });
}

function validateMissingRequiredAndUpdateErrorField(value, errorSelector){
    return validateConditionAndUpdateErrorField(value, 'Value is missing', errorSelector);
}

function validateConditionAndUpdateErrorField(condition, errorMessage, errorSelector){
    if (!condition) {
        $(errorSelector).text(errorMessage);
        return false;
    } else {
        $(errorSelector).text('');
        return true;
    }
}