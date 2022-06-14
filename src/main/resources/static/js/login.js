class Login {
    // element: login box (#login)
    constructor(element) {
        this.element = element;
        this.form = element.find('#form:first');
        this.userInput = element.find('#username:first');
        this.passInput = element.find('#password:first');
        this.spinner = element.find('#login-spinner:first');
        this.error = element.find('#error:first');

        this.form.submit(false);
        this.form.find('#submit-button:first').click($.proxy(this.onSubmit, this));
    }
     
    onSubmit() {
        this.spinner.css('display', 'visible');
        this.error.css('display', 'none');

        let username = this.userInput.val();
        let password = this.passInput.val();

        $.ajax({
            method: 'POST',
            url: '/auth',
            contentType: 'application/json',
            data: JSON.stringify({
                username: username,
                password: password
            }),
            dataType: 'json',
            success: (json) => {
                let error = app.checkError(json);
                if (error !== false) {
                    this.error.html(error);
                    this.error.css('display', 'block');
                } else {
                    app.account = new Account(json.token);
                    app.setup();
                }
            },
            error: (xhr, status, error) => console.error(error)
        });
    }
}