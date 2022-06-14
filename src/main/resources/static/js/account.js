class Account {
    constructor(token) {
        this.token = token;
    }

    disconnect() {
        $.ajax({
            method: 'POST',
            url: `/disconnect?token=${this.token}`,
            success: () => {
                document.location.reload(true); // Reload
            },
            error: (xhr, status, error) => console.error(error)
        })
    }
}