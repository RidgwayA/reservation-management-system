class ApiService {
    constructor() {
        this.baseUrl = '/api';
        this.defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };
    }

    async request(method, url, data = null) {
        const config = {
            method,
            headers: this.defaultHeaders
        };

        if (data && (method === 'POST' || method === 'PUT')) {
            config.body = JSON.stringify(data);
        }

        try {
            const response = await fetch(`${this.baseUrl}${url}`, config);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            if (response.status === 204 || response.headers.get('content-length') === '0') {
                return null;
            }
            
            return await response.json();
        } catch (error) {
            console.error(`API Error [${method} ${url}]:`, error);
            throw error;
        }
    }

    customers = {
        getAll: () => this.request('GET', '/customers'),
        getById: (id) => this.request('GET', `/customers/${id}`),
        search: (term) => this.request('GET', `/customers/search?term=${encodeURIComponent(term)}`),
        create: (customer) => this.request('POST', '/customers', customer),
        update: (id, customer) => this.request('PUT', `/customers/${id}`, customer),
        delete: (id) => this.request('DELETE', `/customers/${id}`)
    };

    campsites = {
        getAll: () => this.request('GET', '/campsites'),
        getById: (id) => this.request('GET', `/campsites/${id}`),
        getAvailable: () => this.request('GET', '/campsites/available'),
        getAvailableByType: (type) => this.request('GET', `/campsites/available/type/${type}`),
        getAvailableForDates: (startDate, endDate, siteType = null) => {
            let url = `/campsites/available/dates?startDate=${startDate}&endDate=${endDate}`;
            if (siteType) url += `&siteType=${siteType}`;
            return this.request('GET', url);
        },
        getBySiteNumber: (siteNumber) => this.request('GET', `/campsites/site/${siteNumber}`),
        markForMaintenance: (id, reason) => this.request('PUT', `/campsites/${id}/maintenance`, reason),
        markAsAvailable: (id) => this.request('PUT', `/campsites/${id}/available`)
    };

    reservations = {
        getAll: () => this.request('GET', '/reservations'),
        getById: (id) => this.request('GET', `/reservations/${id}`),
        getByConfirmation: (confirmationNumber) => this.request('GET', `/reservations/confirmation/${confirmationNumber}`),
        getByCustomer: (customerId) => this.request('GET', `/reservations/customer/${customerId}`),
        getForDateRange: (startDate, endDate) => this.request('GET', `/reservations/dates?startDate=${startDate}&endDate=${endDate}`),
        getTodaysCheckIns: () => this.request('GET', '/reservations/checkin/today'),
        getTodaysCheckOuts: () => this.request('GET', '/reservations/checkout/today'),
        create: (reservation) => this.request('POST', '/reservations', reservation),
        update: (id, reservation) => this.request('PUT', `/reservations/${id}`, reservation),
        checkIn: (id) => this.request('PUT', `/reservations/${id}/checkin`),
        checkOut: (id) => this.request('PUT', `/reservations/${id}/checkout`),
        cancel: (id) => this.request('PUT', `/reservations/${id}/cancel`)
    };

    formatDate(date) {
        if (date instanceof Date) {
            return date.toISOString().split('T')[0];
        }
        return date;
    }

    parseDate(dateString) {
        return new Date(dateString);
    }
}

window.api = new ApiService();