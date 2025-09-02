// Booking Controller - Single Responsibility: Handle booking workflow
class BookingController {
    constructor() {
        this.currentStep = 1;
        this.selectedSite = null;
        this.availableSites = [];
        this.filteredSites = [];
        this.bookingData = {};
        this.currentPage = 1;
        this.sitesPerPage = 10;
        this.selectedLocation = '';
    }

    init() {
        this.setupEventListeners();
        this.setMinDate();
        this.updateStepVisibility();
    }

    setupEventListeners() {
        // Site type selection
        document.querySelectorAll('.site-type-card').forEach(card => {
            card.addEventListener('click', (e) => {
                this.selectSiteType(card.dataset.siteType);
            });
        });

        // Form validation
        document.getElementById('bookingForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.submitBooking();
        });

        // ATV passes toggle
        document.getElementById('needAtvPasses').addEventListener('change', (e) => {
            this.toggleAtvPassDetails(e.target.checked);
        });

        // Party size validation
        document.getElementById('partySize').addEventListener('input', (e) => {
            this.validatePartySize();
        });

        // Site type changes - show/hide RV length
        document.querySelectorAll('input[name="siteType"]').forEach(radio => {
            radio.addEventListener('change', (e) => {
                this.toggleRvLength(e.target.value);
            });
        });

        // Real-time validation
        this.setupRealTimeValidation();

        // Location filter listeners
        document.addEventListener('change', (e) => {
            if (e.target.name === 'locationFilter') {
                this.selectedLocation = e.target.value;
                this.currentPage = 1;
                this.filterAndDisplaySites();
            }
        });
    }

    setupRealTimeValidation() {
        const fields = ['startDate', 'endDate', 'firstName', 'lastName', 'email', 'phone'];
        fields.forEach(fieldName => {
            const field = document.getElementById(fieldName);
            if (field) {
                field.addEventListener('blur', () => this.validateField(field));
                field.addEventListener('input', () => this.clearFieldError(field));
            }
        });
    }

    setMinDate() {
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('startDate').min = today;
        document.getElementById('endDate').min = today;

        // Update end date min when start date changes
        document.getElementById('startDate').addEventListener('change', (e) => {
            const startDate = new Date(e.target.value);
            const nextDay = new Date(startDate);
            nextDay.setDate(nextDay.getDate() + 1);
            document.getElementById('endDate').min = nextDay.toISOString().split('T')[0];
        });
    }

    selectSiteType(siteType) {
        // Update visual selection
        document.querySelectorAll('.site-type-card').forEach(card => {
            card.classList.remove('border-success', 'bg-success', 'text-white');
            card.classList.add('border-light');
        });

        const selectedCard = document.querySelector(`[data-site-type="${siteType}"]`);
        selectedCard.classList.remove('border-light');
        selectedCard.classList.add('border-success', 'bg-success', 'text-white');

        // Update radio button
        document.querySelector(`input[value="${siteType}"]`).checked = true;

        // Show/hide RV length field
        this.toggleRvLength(siteType);

        // Update party size validation
        this.updatePartySizeValidation(siteType);
    }

    toggleRvLength(siteType) {
        const rvLengthGroup = document.getElementById('rvLengthGroup');
        const rvLengthInput = document.getElementById('rvLength');
        
        if (siteType === 'FULL_HOOKUP') {
            rvLengthGroup.style.display = 'block';
            rvLengthInput.required = true;
        } else {
            rvLengthGroup.style.display = 'none';
            rvLengthInput.required = false;
            rvLengthInput.value = '';
        }
    }

    updatePartySizeValidation(siteType) {
        const partySizeInput = document.getElementById('partySize');
        const maxSizes = { 'FULL_HOOKUP': 15, 'TENT': 8 };
        const maxSize = maxSizes[siteType] || 15;
        
        partySizeInput.max = maxSize;
        
        const helpText = partySizeInput.nextElementSibling;
        if (helpText && helpText.classList.contains('form-text')) {
            helpText.textContent = `Maximum ${maxSize} people for ${siteType === 'FULL_HOOKUP' ? 'RV sites' : 'tent sites'}`;
        }
    }

    async checkAvailability() {
        const formData = new FormData(document.getElementById('bookingForm'));
        const startDate = formData.get('startDate');
        const endDate = formData.get('endDate');
        const siteType = formData.get('siteType');

        // Validate required fields
        if (!startDate || !endDate || !siteType) {
            window.notifications.warning('Please fill in all required fields before checking availability.');
            return;
        }

        // Validate date range
        if (new Date(startDate) >= new Date(endDate)) {
            window.notifications.error('Check-out date must be after check-in date.');
            return;
        }

        try {
            window.notifications.info('Checking availability...');
            
            const sites = await api.campsites.getAvailableForDates(startDate, endDate, siteType);
            
            if (sites.length === 0) {
                window.notifications.warning('No sites available for your selected dates. Please try different dates.');
                return;
            }

            this.availableSites = sites;
            this.currentPage = 1;
            this.selectedLocation = '';
            
            // Hide location filters for tent sites since they're all in one location
            const locationFilters = document.getElementById('locationFilters');
            const isTentSite = document.querySelector('input[name="siteType"]:checked').value === 'TENT';
            
            if (isTentSite) {
                locationFilters.parentElement.style.display = 'none';
            } else {
                locationFilters.parentElement.style.display = 'block';
                // Reset to "All Locations" for RV sites
                document.getElementById('allLocations').checked = true;
            }
            
            this.filterAndDisplaySites();
            this.goToStep(2);
            
            window.notifications.success(`Found ${sites.length} available ${siteType.toLowerCase().replace('_', ' ')} sites!`);
            
        } catch (error) {
            console.error('Error checking availability:', error);
            window.notifications.error('Error checking availability. Please try again.');
        }
    }

    displayAvailableSites(sites) {
        const container = document.getElementById('availableSites');
        container.innerHTML = '';

        sites.forEach(site => {
            const siteCard = document.createElement('div');
            siteCard.className = 'col-md-6 col-lg-4 mb-3';
            siteCard.innerHTML = `
                <div class="card site-selection-card" data-site-id="${site.id}" style="cursor: pointer;">
                    <div class="card-body text-center">
                        <h6 class="card-title">Site ${site.siteNumber}</h6>
                        <p class="text-muted mb-1">${site.siteType.replace('_', ' ')}</p>
                        <p class="fw-bold text-success">$${site.dailyRate}/night</p>
                        <small class="text-muted">Max ${site.maxPartySize} people</small>
                    </div>
                </div>
            `;

            siteCard.addEventListener('click', () => this.selectSite(site));
            container.appendChild(siteCard);
        });
    }

    filterAndDisplaySites() {
        // Filter sites by location
        this.filteredSites = this.selectedLocation ? 
            this.availableSites.filter(site => site.location === this.selectedLocation) : 
            this.availableSites;

        // Calculate pagination
        const totalPages = Math.ceil(this.filteredSites.length / this.sitesPerPage);
        const startIndex = (this.currentPage - 1) * this.sitesPerPage;
        const endIndex = startIndex + this.sitesPerPage;
        const sitesToShow = this.filteredSites.slice(startIndex, endIndex);

        // Display sites
        this.displaySitesPage(sitesToShow);
        
        // Update pagination controls
        this.updatePaginationControls(totalPages);
    }

    displaySitesPage(sites) {
        const container = document.getElementById('availableSites');
        container.innerHTML = '';

        if (sites.length === 0) {
            container.innerHTML = `
                <div class="col-12 text-center">
                    <div class="alert alert-info">
                        <i class="bi bi-info-circle me-2"></i>
                        No sites available in the selected location. Try a different location.
                    </div>
                </div>
            `;
            return;
        }

        sites.forEach(site => {
            const siteCard = document.createElement('div');
            siteCard.className = 'col-12 col-sm-6 col-md-4 col-lg-3 mb-3';
            siteCard.innerHTML = `
                <div class="card site-selection-card h-100" data-site-id="${site.id}" style="cursor: pointer;">
                    <div class="card-body text-center d-flex flex-column justify-content-between">
                        <div>
                            <h6 class="card-title">Site ${site.siteNumber}</h6>
                            <p class="text-muted mb-1">${site.siteType.replace('_', ' ')}</p>
                            <small class="text-info mb-1">${site.location.replace('_', ' ')}</small>
                        </div>
                        <div>
                            <p class="fw-bold text-success mb-1">$${site.dailyRate}/night</p>
                            <small class="text-muted">Max ${site.maxPartySize} people</small>
                        </div>
                    </div>
                </div>
            `;

            siteCard.addEventListener('click', () => this.selectSite(site));
            container.appendChild(siteCard);
        });
    }

    updatePaginationControls(totalPages) {
        const paginationControls = document.getElementById('paginationControls');
        
        if (totalPages <= 1) {
            paginationControls.style.display = 'none';
            return;
        }

        paginationControls.style.display = 'block';
        
        // Update page numbers - insert between prev and next buttons
        const pageNumbers = document.getElementById('pageNumbers');
        const nextButton = document.getElementById('nextPage');
        
        // Remove existing page number items (but keep prev/next buttons)
        const existingPageNumbers = pageNumbers.querySelectorAll('.page-item:not(#prevPage):not(#nextPage)');
        existingPageNumbers.forEach(item => item.remove());
        
        // Insert new page numbers before the next button
        for (let i = 1; i <= totalPages; i++) {
            const pageItem = document.createElement('li');
            pageItem.className = `page-item ${i === this.currentPage ? 'active' : ''}`;
            pageItem.innerHTML = `<a class="page-link" href="#" data-page="${i}">${i}</a>`;
            pageNumbers.insertBefore(pageItem, nextButton);
        }

        // Update prev/next buttons
        document.getElementById('prevPage').classList.toggle('disabled', this.currentPage === 1);
        document.getElementById('nextPage').classList.toggle('disabled', this.currentPage === totalPages);

        // Add click listeners for pagination
        document.querySelectorAll('.page-link[data-page]').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.currentPage = parseInt(e.target.dataset.page);
                this.filterAndDisplaySites();
            });
        });

        // Prev/next buttons
        document.getElementById('prevPage').querySelector('.page-link').onclick = (e) => {
            e.preventDefault();
            if (this.currentPage > 1) {
                this.currentPage--;
                this.filterAndDisplaySites();
            }
        };

        document.getElementById('nextPage').querySelector('.page-link').onclick = (e) => {
            e.preventDefault();
            if (this.currentPage < totalPages) {
                this.currentPage++;
                this.filterAndDisplaySites();
            }
        };
    }

    selectSite(site) {
        // Update visual selection
        document.querySelectorAll('.site-selection-card').forEach(card => {
            card.classList.remove('border-success', 'bg-light');
        });

        const selectedCard = document.querySelector(`[data-site-id="${site.id}"]`);
        selectedCard.classList.add('border-success', 'bg-light');

        this.selectedSite = site;
        document.getElementById('continueToStep3').disabled = false;

        window.notifications.success(`Selected Site ${site.siteNumber}`);
    }

    validatePartySize() {
        const partySizeInput = document.getElementById('partySize');
        const partySize = parseInt(partySizeInput.value);
        const maxSize = parseInt(partySizeInput.max);

        if (partySize > maxSize) {
            partySizeInput.classList.add('is-invalid');
            const feedback = partySizeInput.parentNode.querySelector('.invalid-feedback') || 
                           document.createElement('div');
            feedback.className = 'invalid-feedback';
            feedback.textContent = `Party size cannot exceed ${maxSize} for this site type.`;
            if (!partySizeInput.parentNode.querySelector('.invalid-feedback')) {
                partySizeInput.parentNode.appendChild(feedback);
            }
            return false;
        } else {
            partySizeInput.classList.remove('is-invalid');
            return true;
        }
    }

    toggleAtvPassDetails(show) {
        const container = document.getElementById('atvPassDetails');
        if (show) {
            container.classList.remove('d-none');
        } else {
            container.classList.add('d-none');
            // Reset ATV pass counts
            document.getElementById('atvAdults').value = 0;
            document.getElementById('atvTeens').value = 0;
            document.getElementById('atvChildren').value = 0;
        }
        this.updateBookingSummary();
    }

    updateBookingSummary() {
        const summaryContainer = document.getElementById('bookingSummary');
        if (!summaryContainer) return;

        const formData = new FormData(document.getElementById('bookingForm'));
        const startDate = formData.get('startDate');
        const endDate = formData.get('endDate');

        if (!startDate || !endDate || !this.selectedSite) return;

        const nights = this.calculateNights(startDate, endDate);
        const siteTotal = nights * this.selectedSite.dailyRate;
        
        let atvTotal = 0;
        if (document.getElementById('needAtvPasses').checked) {
            const adults = parseInt(formData.get('atvAdults') || 0);
            const teens = parseInt(formData.get('atvTeens') || 0);
            atvTotal = (adults * 20 + teens * 10) * nights;
        }

        const total = siteTotal + atvTotal;
        const deposit = Math.ceil(total * 0.25); // 25% deposit

        summaryContainer.innerHTML = `
            <div class="row">
                <div class="col-md-6">
                    <strong>Site ${this.selectedSite.siteNumber}</strong><br>
                    <small class="text-muted">${this.selectedSite.siteType.replace('_', ' ')}</small>
                </div>
                <div class="col-md-6 text-end">
                    ${nights} nights × $${this.selectedSite.dailyRate} = <strong>$${siteTotal}</strong>
                </div>
            </div>
            ${atvTotal > 0 ? `
            <div class="row mt-2">
                <div class="col-md-6">
                    ATV Passes<br>
                    <small class="text-muted">${nights} days</small>
                </div>
                <div class="col-md-6 text-end">
                    <strong>$${atvTotal}</strong>
                </div>
            </div>
            ` : ''}
            <hr>
            <div class="row">
                <div class="col-md-6">
                    <strong>Total Amount</strong>
                </div>
                <div class="col-md-6 text-end">
                    <strong>$${total}</strong>
                </div>
            </div>
            <div class="row text-success">
                <div class="col-md-6">
                    <strong>Required Deposit (25%)</strong>
                </div>
                <div class="col-md-6 text-end">
                    <strong>$${deposit}</strong>
                </div>
            </div>
        `;
    }

    calculateNights(startDate, endDate) {
        const start = new Date(startDate);
        const end = new Date(endDate);
        const diffTime = Math.abs(end - start);
        return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    }

    validateField(field) {
        const value = field.value.trim();
        let isValid = true;
        let errorMessage = '';

        switch (field.name) {
            case 'email':
                isValid = validator.isValidEmail(value);
                errorMessage = 'Please enter a valid email address.';
                break;
            case 'phone':
            case 'emergencyPhone':
                isValid = validator.isValidPhone(value);
                errorMessage = 'Please enter a valid phone number.';
                break;
            case 'startDate':
                isValid = validator.isFutureDate(value);
                errorMessage = 'Check-in date must be today or in the future.';
                break;
            case 'endDate':
                isValid = validator.isStrictlyFutureDate(value);
                errorMessage = 'Check-out date must be in the future.';
                break;
        }

        if (!isValid) {
            field.classList.add('is-invalid');
            let feedback = field.parentNode.querySelector('.invalid-feedback');
            if (!feedback) {
                feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                field.parentNode.appendChild(feedback);
            }
            feedback.textContent = errorMessage;
        } else {
            field.classList.remove('is-invalid');
        }

        return isValid;
    }

    clearFieldError(field) {
        field.classList.remove('is-invalid');
    }

    goToStep(stepNumber) {
        // Validate current step before proceeding
        if (stepNumber > this.currentStep && !this.validateCurrentStep()) {
            return;
        }

        this.currentStep = stepNumber;
        this.updateStepVisibility();
        
        // Update summary if going to step 5
        if (stepNumber === 5) {
            this.updateBookingSummary();
        }
    }

    validateCurrentStep() {
        switch (this.currentStep) {
            case 1:
                const startDate = document.getElementById('startDate').value;
                const endDate = document.getElementById('endDate').value;
                const siteType = document.querySelector('input[name="siteType"]:checked');
                
                if (!startDate || !endDate || !siteType) {
                    window.notifications.warning('Please complete all required fields.');
                    return false;
                }
                
                if (new Date(startDate) >= new Date(endDate)) {
                    window.notifications.error('Check-out date must be after check-in date.');
                    return false;
                }
                break;

            case 2:
                if (!this.selectedSite) {
                    window.notifications.warning('Please select a campsite.');
                    return false;
                }
                break;

            case 3:
                const requiredFields = ['firstName', 'lastName', 'email', 'phone', 'emergencyName', 'emergencyPhone'];
                let isValid = true;
                
                requiredFields.forEach(fieldName => {
                    const field = document.getElementById(fieldName);
                    if (!this.validateField(field) || !field.value.trim()) {
                        isValid = false;
                    }
                });
                
                if (!isValid) {
                    window.notifications.warning('Please complete all required customer information.');
                    return false;
                }
                break;

            case 4:
                const partySize = document.getElementById('partySize').value;
                const licensePlate = document.getElementById('licensePlate').value;
                const siteTypeValue = document.querySelector('input[name="siteType"]:checked').value;
                const rvLength = document.getElementById('rvLength').value;
                
                if (!partySize || !licensePlate) {
                    window.notifications.warning('Please complete party size and vehicle information.');
                    return false;
                }
                
                if (siteTypeValue === 'FULL_HOOKUP' && !rvLength) {
                    window.notifications.warning('Please provide RV length for RV sites.');
                    return false;
                }
                
                if (!this.validatePartySize()) {
                    return false;
                }
                break;
        }
        
        return true;
    }

    updateStepVisibility() {
        // Hide all steps
        document.querySelectorAll('.booking-step').forEach(step => {
            step.classList.add('d-none');
        });

        // Show current step
        document.getElementById(`step${this.currentStep}`).classList.remove('d-none');

        // Scroll to top of form
        document.querySelector('.card-header').scrollIntoView({ behavior: 'smooth' });
    }

    async submitBooking() {
        if (!this.validateCurrentStep()) {
            return;
        }

        try {
            const bookingData = this.gatherBookingData();
            
            window.notifications.info('Processing your booking...');

            // Create the reservation with customer data
            const reservation = await this.createReservation(bookingData);
            
            window.notifications.success('Booking completed successfully! Confirmation email sent.');
            
            // Redirect or show success page
            this.showBookingSuccess(reservation);
            
        } catch (error) {
            console.error('Booking error:', error);
            window.notifications.error('Booking failed. Please try again or contact us for assistance.');
        }
    }

    gatherBookingData() {
        const formData = new FormData(document.getElementById('bookingForm'));
        const data = {};
        
        // Convert FormData to regular object
        for (let [key, value] of formData.entries()) {
            data[key] = value;
        }
        
        // Add selected site
        data.selectedSite = this.selectedSite;
        
        // Process party members
        if (data.partyMembers) {
            data.partyMembersList = data.partyMembers.split('\n')
                .filter(member => member.trim())
                .map(member => member.trim());
        }
        
        // Ensure at least one party member (use customer name if none provided)
        if (!data.partyMembersList || data.partyMembersList.length === 0) {
            data.partyMembersList = [`${data.firstName} ${data.lastName}`];
        }
        
        return data;
    }

    async createReservation(bookingData) {
        // Build reservation DTO
        const reservationDto = {
            customer: {
                firstName: bookingData.firstName,
                lastName: bookingData.lastName,
                email: bookingData.email,
                phone: bookingData.phone,
                emergencyContactName: bookingData.emergencyContactName,
                emergencyContactRelationship: bookingData.emergencyContactRelationship,
                emergencyEmail: bookingData.emergencyEmail,
                emergencyPhone: bookingData.emergencyPhone
            },
            campsite: {
                id: bookingData.selectedSite.id
            },
            startDate: bookingData.startDate,
            endDate: bookingData.endDate,
            partySize: parseInt(bookingData.partySize),
            partyMembers: bookingData.partyMembersList || [],
            vehicleLicensePlate: bookingData.vehicleLicensePlate,
            vehicleMake: bookingData.vehicleMake,
            vehicleModel: bookingData.vehicleModel,
            rvLengthFeet: bookingData.rvLength ? parseInt(bookingData.rvLength) : null,
            notes: bookingData.notes || '',
            status: 'PENDING'
        };

        // Calculate totals (this should ideally be done server-side)
        const nights = Math.ceil((new Date(bookingData.endDate) - new Date(bookingData.startDate)) / (1000 * 60 * 60 * 24));
        const campsiteTotal = bookingData.selectedSite.dailyRate * nights;
        const atvTotal = bookingData.needAtvPasses === 'true' ? (parseInt(bookingData.atvPassCount) || 0) * 10 : 0;
        const totalAmount = campsiteTotal + atvTotal;

        reservationDto.campsiteTotal = campsiteTotal;
        reservationDto.atvTotal = atvTotal;
        reservationDto.totalAmount = totalAmount;
        reservationDto.paidAmount = 0; // Not paid yet

        // Create the reservation
        console.log('Sending reservation DTO:', JSON.stringify(reservationDto, null, 2));
        
        const reservation = await window.api.reservations.create(reservationDto);
        
        console.log('Created reservation:', reservation);
        return reservation;
    }

    showBookingSuccess(reservation) {
        const confirmationNumber = reservation.confirmationNumber || 'RV' + Date.now();
        
        document.querySelector('main').innerHTML = `
            <div class="container my-5 text-center">
                <div class="row justify-content-center">
                    <div class="col-lg-6">
                        <div class="card shadow-lg border-success">
                            <div class="card-body p-5">
                                <i class="bi bi-check-circle text-success" style="font-size: 4rem;"></i>
                                <h2 class="text-success mt-3">Booking Confirmed!</h2>
                                <p class="lead">Your reservation has been successfully created.</p>
                                
                                <div class="alert alert-success">
                                    <h5>Confirmation Number</h5>
                                    <h3 class="mb-0">${confirmationNumber}</h3>
                                </div>
                                
                                <div class="text-start mt-4">
                                    <h6>Next Steps:</h6>
                                    <ol>
                                        <li>Check your email for confirmation details</li>
                                        <li>Arrive at check-in: 1:00 PM on ${reservation.startDate}</li>
                                        <li>Visit the front office before parking</li>
                                        <li>Purchase ATV passes at the gate house if needed</li>
                                    </ol>
                                </div>
                                
                                <div class="mt-4">
                                    <a href="/" class="btn btn-success me-2">Book Another Stay</a>
                                    <a href="/reservations/lookup" class="btn btn-outline-success">View Reservation</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }
}

// Create global instance
window.BookingController = new BookingController();