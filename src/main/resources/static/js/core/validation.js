class ValidationService {
    constructor() {
        this.rules = {};
    }

    isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    isValidPhone(phone) {
        const phoneRegex = /^[\+]?[1-9][\d]{1,14}$/;
        const cleaned = phone.replace(/[\s\-\(\)]/g, '');
        return phoneRegex.test(cleaned);
    }

    isValidDate(date) {
        const dateObj = new Date(date);
        return dateObj instanceof Date && !isNaN(dateObj);
    }

    isFutureDate(date) {
        const [year, month, day] = date.split('-').map(Number);
        const dateObj = new Date(year, month - 1, day);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return dateObj >= today;
    }

    isStrictlyFutureDate(date) {
        const [year, month, day] = date.split('-').map(Number);
        const dateObj = new Date(year, month - 1, day);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return dateObj > today;
    }

    isValidDateRange(startDate, endDate) {
        const start = new Date(startDate);
        const end = new Date(endDate);
        return start <= end;
    }

    isValidPartySize(size, siteType) {
        const maxSizes = {
            'FULL_HOOKUP': 15,
            'TENT': 8
        };
        return size > 0 && size <= (maxSizes[siteType] || 15);
    }

    isValidLicensePlate(plate) {
        const plateRegex = /^[A-Z0-9\-\s]{2,10}$/i;
        return plateRegex.test(plate);
    }

    isValidRvLength(length) {
        return length >= 10 && length <= 100;
    }

    validateForm(formData, rules) {
        const errors = {};

        Object.keys(rules).forEach(field => {
            const value = formData[field];
            const fieldRules = rules[field];

            if (fieldRules.required && (!value || value.trim() === '')) {
                errors[field] = fieldRules.required;
                return;
            }

            if (value && fieldRules.email && !this.isValidEmail(value)) {
                errors[field] = fieldRules.email;
            }

            if (value && fieldRules.phone && !this.isValidPhone(value)) {
                errors[field] = fieldRules.phone;
            }

            if (value && fieldRules.minLength && value.length < fieldRules.minLength) {
                errors[field] = `Must be at least ${fieldRules.minLength} characters`;
            }

            if (value && fieldRules.maxLength && value.length > fieldRules.maxLength) {
                errors[field] = `Must not exceed ${fieldRules.maxLength} characters`;
            }

            if (value && fieldRules.min && Number(value) < fieldRules.min) {
                errors[field] = `Must be at least ${fieldRules.min}`;
            }

            if (value && fieldRules.max && Number(value) > fieldRules.max) {
                errors[field] = `Must not exceed ${fieldRules.max}`;
            }
        });

        return errors;
    }

    displayErrors(errors) {
        document.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
        });
        document.querySelectorAll('.invalid-feedback').forEach(el => {
            el.remove();
        });

        Object.keys(errors).forEach(field => {
            const input = document.querySelector(`[name="${field}"]`);
            if (input) {
                input.classList.add('is-invalid');
                
                const feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                feedback.textContent = errors[field];
                
                input.parentNode.appendChild(feedback);
            }
        });
    }

    clearErrors() {
        document.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
        });
        document.querySelectorAll('.invalid-feedback').forEach(el => {
            el.remove();
        });
    }
}

window.validator = new ValidationService();