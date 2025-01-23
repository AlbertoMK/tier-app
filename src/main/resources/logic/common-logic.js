function httpRequest(url, method = 'GET', body = null) {
    return fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: body
    })
        .then(response => {
            if (response.headers.get('Content-Type').includes('application/json')) {
                return response.json().then(data => {
                    return { status: response.status, body: data };
                });
            } else {
                return response.text().then(text => {
                    return { status: response.status, body: text };
                });
            }
        });
}
