function httpRequest(url, method = 'GET', body = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body !== null && method !== 'GET' && method !== 'HEAD') {
        options.body = body;
    }
    return fetch(url, options)
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
