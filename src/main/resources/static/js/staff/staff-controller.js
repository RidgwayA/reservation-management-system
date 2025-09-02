class StaffController {
    constructor() {
        this.reservations = [];
        this.campsites = [];
        this.checkins = [];
        this.checkouts = [];
    }

    async init() {
        try {
            console.log('Initializing StaffController...');
            await this.loadDashboardData();
            console.log('Data loaded - reservations:', this.reservations.length, 'campsites:', this.campsites.length);
            this.updateStats();
            this.setupEventListeners();
            this.populateCheckinsTable();
            console.log('StaffController initialization complete');
        } catch (error) {
            console.error('Error initializing staff dashboard:', error);
            window.notifications.error('Error loading dashboard data.');
        }
    }

    setupEventListeners() {
        console.log('Setting up event listeners...');
        
        const tabs = document.querySelectorAll('[data-bs-toggle="tab"]');
        console.log('Found tabs:', tabs.length);
        
        tabs.forEach(tab => {
            console.log('Setting up listener for tab:', tab.getAttribute('data-bs-target'));
            tab.addEventListener('shown.bs.tab', (e) => {
                const targetTab = e.target.getAttribute('data-bs-target');
                console.log('Tab changed to:', targetTab);
                this.onTabChange(targetTab);
            });
        });

        document.getElementById('filterDate').value = new Date().toISOString().split('T')[0];
    }

    async loadDashboardData() {
        try {
            const today = new Date().toISOString().split('T')[0];
            
            const [reservations, campsites, checkins, checkouts, availableSites] = await Promise.all([
                api.reservations.getAll(),
                api.campsites.getAll(),
                api.reservations.getTodaysCheckIns(),
                api.reservations.getTodaysCheckOuts(),
                api.campsites.getAvailableForDates(today, today)
            ]);

            this.reservations = reservations || [];
            this.campsites = campsites || [];
            this.checkins = checkins || [];
            this.checkouts = checkouts || [];
            this.availableSites = availableSites || [];

        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.reservations = [];
            this.campsites = [];
            this.checkins = [];
            this.checkouts = [];
            this.availableSites = [];
        }
    }

    updateStats() {
        document.getElementById('todaysCheckIns').textContent = this.checkins.length;
        document.getElementById('todaysCheckOuts').textContent = this.checkouts.length;
        
        const availableSitesCount = this.availableSites.length;
        document.getElementById('availableSitesCount').textContent = availableSitesCount;
    }

    onTabChange(targetTab) {
        console.log('onTabChange called with:', targetTab);
        switch (targetTab) {
            case '#checkin':
                this.populateCheckinsTable();
                break;
            case '#checkout':
                this.populateCheckoutsTable();
                break;
            case '#reservations':
                this.populateReservationsTable();
                break;
            case '#sites':
                console.log('Sites tab selected - calling populateSitesTable()');
                this.populateSitesTable();
                break;
            default:
                console.log('Unknown tab:', targetTab);
        }
    }

    populateCheckinsTable() {
        const tbody = document.getElementById('checkinTableBody');
        
        if (this.checkins.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No check-ins scheduled for today</td></tr>';
            return;
        }

        tbody.innerHTML = this.checkins.map(reservation => `
            <tr>
                <td>
                    <strong>${reservation.confirmationNumber || 'N/A'}</strong>
                </td>
                <td>
                    ${reservation.customer ? reservation.customer.fullName : 'N/A'}
                </td>
                <td>
                    <span class="badge bg-success">Site ${reservation.campsite ? reservation.campsite.siteNumber : 'N/A'}</span>
                </td>
                <td>
                    <i class="bi bi-people me-1"></i>${reservation.partySize || 0}
                </td>
                <td>
                    <span class="badge bg-${this.getStatusColor(reservation.status)}">${reservation.status}</span>
                </td>
                <td>
                    ${reservation.status === 'CONFIRMED' ? 
                        `<button class="btn btn-success btn-sm" onclick="window.StaffController.checkInReservation(${reservation.id})">
                            <i class="bi bi-box-arrow-in-right me-1"></i>Check In
                        </button>` : 
                        `<button class="btn btn-outline-secondary btn-sm" disabled>
                            Already Checked In
                        </button>`
                    }
                </td>
            </tr>
        `).join('');
    }

    populateCheckoutsTable() {
        const tbody = document.getElementById('checkoutTableBody');
        
        if (this.checkouts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No check-outs scheduled for today</td></tr>';
            return;
        }

        tbody.innerHTML = this.checkouts.map(reservation => `
            <tr>
                <td>
                    <strong>${reservation.confirmationNumber || 'N/A'}</strong>
                </td>
                <td>
                    ${reservation.customer ? reservation.customer.fullName : 'N/A'}
                </td>
                <td>
                    <span class="badge bg-warning text-dark">Site ${reservation.campsite ? reservation.campsite.siteNumber : 'N/A'}</span>
                </td>
                <td>
                    ${reservation.checkOutTime ? new Date(reservation.checkOutTime).toLocaleTimeString() : 'Not checked out'}
                </td>
                <td>
                    <span class="badge bg-${this.getStatusColor(reservation.status)}">${reservation.status}</span>
                </td>
                <td>
                    ${reservation.status === 'CHECKED_IN' ? 
                        `<button class="btn btn-warning btn-sm" onclick="window.StaffController.checkOutReservation(${reservation.id})">
                            <i class="bi bi-box-arrow-right me-1"></i>Check Out
                        </button>` : 
                        `<button class="btn btn-outline-secondary btn-sm" disabled>
                            Completed
                        </button>`
                    }
                </td>
            </tr>
        `).join('');
    }

    populateReservationsTable() {
        const tbody = document.getElementById('reservationsTableBody');
        
        if (this.reservations.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No reservations found</td></tr>';
            return;
        }

        tbody.innerHTML = this.reservations.map(reservation => `
            <tr>
                <td>
                    <strong>${reservation.confirmationNumber || 'N/A'}</strong>
                </td>
                <td>
                    ${reservation.customer ? reservation.customer.fullName : 'N/A'}
                    <br><small class="text-muted">${reservation.customer ? reservation.customer.email : ''}</small>
                </td>
                <td>
                    <span class="badge bg-info">Site ${reservation.campsite ? reservation.campsite.siteNumber : 'N/A'}</span>
                </td>
                <td>
                    <small>
                        ${reservation.startDate || 'N/A'} to<br>
                        ${reservation.endDate || 'N/A'}
                    </small>
                </td>
                <td>
                    <i class="bi bi-people me-1"></i>${reservation.partySize || 0}
                </td>
                <td>
                    <span class="badge bg-${this.getStatusColor(reservation.status)}">${reservation.status}</span>
                </td>
                <td>
                    <div class="btn-group btn-group-sm" role="group">
                        <button class="btn btn-outline-primary btn-sm" onclick="window.StaffController.viewReservation(${reservation.id})" title="View Details">
                            <i class="bi bi-eye"></i>
                        </button>
                        ${reservation.status === 'CONFIRMED' || reservation.status === 'CHECKED_IN' ? 
                            `<button class="btn btn-outline-danger btn-sm" onclick="window.StaffController.cancelReservation(${reservation.id})" title="Cancel">
                                <i class="bi bi-x-circle"></i>
                            </button>` : ''
                        }
                    </div>
                </td>
            </tr>
        `).join('');
    }

    populateSitesTable() {
        console.log('populateSitesTable() called');
        console.log('this.campsites.length:', this.campsites.length);
        console.log('this.reservations.length:', this.reservations.length);
        
        const tbody = document.getElementById('sitesTableBody');
        
        if (this.campsites.length === 0) {
            console.log('No campsites found, showing empty message');
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No campsites found</td></tr>';
            return;
        }

        tbody.innerHTML = this.campsites.map(site => {
            const actualStatus = this.getSiteActualStatus(site);
            
            return `
            <tr>
                <td>
                    <strong>${site.siteNumber}</strong>
                </td>
                <td>
                    <span class="badge bg-${site.siteType === 'FULL_HOOKUP' ? 'primary' : 'secondary'}">
                        ${site.siteType === 'FULL_HOOKUP' ? 'RV Site' : 'Tent Site'}
                    </span>
                </td>
                <td>
                    <span class="badge bg-${this.getSiteStatusColor(actualStatus)}">${actualStatus}</span>
                </td>
                <td>
                    $${site.dailyRate}/night
                </td>
                <td>
                    ${site.maxPartySize} people
                </td>
                <td>
                    <div class="btn-group btn-group-sm" role="group">
                        ${site.status === 'AVAILABLE' ? 
                            `<button class="btn btn-outline-warning btn-sm" onclick="window.StaffController.markSiteForMaintenance(${site.id})" title="Maintenance">
                                <i class="bi bi-tools"></i>
                            </button>` :
                            `<button class="btn btn-outline-success btn-sm" onclick="window.StaffController.markSiteAvailable(${site.id})" title="Mark Available">
                                <i class="bi bi-check-circle"></i>
                            </button>`
                        }
                    </div>
                </td>
            </tr>
            `;
        }).join('');
    }

    getStatusColor(status) {
        const colors = {
            'PENDING': 'warning',
            'CONFIRMED': 'success',
            'CHECKED_IN': 'primary',
            'COMPLETED': 'secondary',
            'CANCELLED': 'danger'
        };
        return colors[status] || 'secondary';
    }

    getSiteActualStatus(site) {
        if (site.status === 'MAINTENANCE' || site.status === 'OUT_OF_ORDER') {
            return site.status;
        }

        const today = new Date().toISOString().split('T')[0];
        console.log(`Checking site ${site.siteNumber}, today is ${today}`);
        
        const siteReservations = this.reservations.filter(reservation => {
            if (reservation.campsite && reservation.campsite.id === site.id) {
                console.log(`Found reservation for site ${site.siteNumber}: status=${reservation.status}, start=${reservation.startDate}, end=${reservation.endDate}`);
                if (['CONFIRMED', 'CHECKED_IN'].includes(reservation.status)) {
                    const startDate = reservation.startDate;
                    const endDate = reservation.endDate;
                    
                    const overlaps = startDate <= today && endDate >= today;
                    console.log(`  Date overlap check: ${startDate} <= ${today} <= ${endDate} = ${overlaps}`);
                    return overlaps;
                }
            }
            return false;
        });

        console.log(`Site ${site.siteNumber} has ${siteReservations.length} active reservations`);

        if (siteReservations.length > 0) {
            if (siteReservations.some(r => r.status === 'CHECKED_IN')) {
                console.log(`Site ${site.siteNumber} is OCCUPIED`);
                return 'OCCUPIED';
            }
            if (siteReservations.some(r => r.status === 'CONFIRMED')) {
                console.log(`Site ${site.siteNumber} is RESERVED`);
                return 'RESERVED';
            }
        }

        console.log(`Site ${site.siteNumber} is AVAILABLE`);
        return 'AVAILABLE';
    }

    getSiteStatusColor(status) {
        const colors = {
            'AVAILABLE': 'success',
            'OCCUPIED': 'danger', 
            'RESERVED': 'warning',
            'MAINTENANCE': 'danger',
            'OUT_OF_ORDER': 'dark'
        };
        return colors[status] || 'secondary';
    }

    async checkInReservation(reservationId) {
        try {
            await api.reservations.checkIn(reservationId);
            window.notifications.success('Guest checked in successfully!');
            await this.refreshData();
        } catch (error) {
            console.error('Check-in error:', error);
            window.notifications.error('Error checking in guest. Please try again.');
        }
    }

    async checkOutReservation(reservationId) {
        try {
            await api.reservations.checkOut(reservationId);
            window.notifications.success('Guest checked out successfully!');
            await this.refreshData();
        } catch (error) {
            console.error('Check-out error:', error);
            window.notifications.error('Error checking out guest. Please try again.');
        }
    }

    async cancelReservation(reservationId) {
        if (!confirm('Are you sure you want to cancel this reservation?')) {
            return;
        }

        try {
            await api.reservations.cancel(reservationId);
            window.notifications.success('Reservation cancelled successfully!');
            await this.refreshData();
        } catch (error) {
            console.error('Cancellation error:', error);
            window.notifications.error('Error cancelling reservation. Please try again.');
        }
    }

    async markSiteForMaintenance(siteId) {
        const reason = prompt('Enter maintenance reason:');
        if (!reason) return;

        try {
            await api.campsites.markForMaintenance(siteId, reason);
            window.notifications.success('Site marked for maintenance.');
            await this.refreshData();
        } catch (error) {
            console.error('Maintenance error:', error);
            window.notifications.error('Error updating site status. Please try again.');
        }
    }

    async markSiteAvailable(siteId) {
        try {
            await api.campsites.markAsAvailable(siteId);
            window.notifications.success('Site marked as available.');
            await this.refreshData();
        } catch (error) {
            console.error('Site update error:', error);
            window.notifications.error('Error updating site status. Please try again.');
        }
    }

    viewReservation(reservationId) {
        const reservation = this.reservations.find(r => r.id === reservationId);
        if (!reservation) {
            window.notifications.error('Reservation not found.');
            return;
        }

        const details = `
Confirmation: ${reservation.confirmationNumber || 'N/A'}
Guest: ${reservation.customer ? reservation.customer.fullName : 'N/A'}
Email: ${reservation.customer ? reservation.customer.email : 'N/A'}
Site: ${reservation.campsite ? reservation.campsite.siteNumber : 'N/A'}
Dates: ${reservation.startDate || 'N/A'} to ${reservation.endDate || 'N/A'}
Party Size: ${reservation.partySize || 0} people
Status: ${reservation.status}
        `;

        alert('Reservation Details:\n\n' + details);
    }

    async refreshCheckIns() {
        try {
            this.checkins = await api.reservations.getTodaysCheckIns();
            this.populateCheckinsTable();
            this.updateStats();
            window.notifications.success('Check-ins refreshed.');
        } catch (error) {
            console.error('Refresh error:', error);
            window.notifications.error('Error refreshing check-ins.');
        }
    }

    async refreshCheckOuts() {
        try {
            this.checkouts = await api.reservations.getTodaysCheckOuts();
            this.populateCheckoutsTable();
            this.updateStats();
            window.notifications.success('Check-outs refreshed.');
        } catch (error) {
            console.error('Refresh error:', error);
            window.notifications.error('Error refreshing check-outs.');
        }
    }

    async refreshData() {
        await this.loadDashboardData();
        this.updateStats();
        
        const activeTab = document.querySelector('[data-bs-toggle="tab"].active');
        if (activeTab) {
            this.onTabChange(activeTab.getAttribute('data-bs-target'));
        }
    }

    async filterReservations() {
        const filterDate = document.getElementById('filterDate').value;
        if (!filterDate) {
            this.populateReservationsTable();
            return;
        }

        try {
            const filtered = await api.reservations.getForDateRange(filterDate, filterDate);
            const originalReservations = this.reservations;
            this.reservations = filtered;
            this.populateReservationsTable();
            this.reservations = originalReservations;
            
            window.notifications.info(`Showing reservations for ${filterDate}`);
        } catch (error) {
            console.error('Filter error:', error);
            window.notifications.error('Error filtering reservations.');
        }
    }

    filterSites() {
        const siteTypeFilter = document.getElementById('siteTypeFilter').value;
        
        let filteredSites = this.campsites;
        if (siteTypeFilter) {
            filteredSites = this.campsites.filter(site => site.siteType === siteTypeFilter);
        }

        const originalSites = this.campsites;
        this.campsites = filteredSites;
        this.populateSitesTable();
        this.campsites = originalSites;
        
        window.notifications.info(`Showing ${siteTypeFilter ? siteTypeFilter.replace('_', ' ').toLowerCase() : 'all'} sites`);
    }
}

console.log('Creating StaffController instance...');
window.StaffController = new StaffController();
console.log('StaffController instance created:', window.StaffController);