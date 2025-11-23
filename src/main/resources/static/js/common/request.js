const Request = {
    baseUrl: '', // Can be set if needed, relative paths work fine usually

    request: function(url, method, data) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: url,
                type: method,
                contentType: 'application/json',
                data: method === 'GET' ? data : JSON.stringify(data),
                success: function(response) {
                    resolve(response);
                },
                error: function(xhr, status, error) {
                    console.error('Request failed:', error);
                    resolve({ code: 500, msg: 'Network error: ' + error });
                }
            });
        });
    },

    get: function(url, params) {
        return this.request(url, 'GET', params);
    },

    post: function(url, data) {
        return this.request(url, 'POST', data);
    },

    put: function(url, data) {
        return this.request(url, 'PUT', data);
    },

    delete: function(url, data) {
        return this.request(url, 'DELETE', data);
    }
};
