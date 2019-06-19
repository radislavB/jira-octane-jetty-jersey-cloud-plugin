function hostAjaxGet(url) {
    return hostAjaxSend('GET', url, "");
}

function hostAjaxDelete(url) {
    return hostAjaxSend('DELETE', url, "");
}

function hostAjaxPost(url, data) {
    return hostAjaxSend('POST', url, data);
}

function hostAjaxPut(url, data) {
    return hostAjaxSend('PUT', url, data);
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
                    console.log("error", requestType, url, "status : ", xhr.statusText, "message : ", xhr.responseText);
                    reject(Error(!xhr.responseText ? xhr.statusText : xhr.responseText));
                }
            });
        });
    });
}

function showFlag(text, type) {
    if (!type) {
        type = 'success';
    }
    var close = (type === "success") ? "auto" : "manual";
    var flag = AP.flag.create({
        close: close,
        body: text,
        type: type
    });
}

function validateMissingRequiredAndUpdateErrorField(value, errorSelector) {
    return validateConditionAndUpdateErrorField(value, 'Value is missing', errorSelector);
}

function validateConditionAndUpdateErrorField(condition, errorMessage, errorSelector) {
    if (!condition) {
        $(errorSelector).text(errorMessage);
        return false;
    } else {
        $(errorSelector).text('');
        return true;
    }
}