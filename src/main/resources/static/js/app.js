class Application {
    // Login: Login instance
    // element: $('#application')
    constructor(login, element) {
        this.login = login;
        this.element = element;
        this.account = null;
        this.postits = [];

        // Get colors codes list (as a JSON array)
        $.getJSON('/errors', (json) => {
            this.errors = json;
        })

        // Get colors
        $.getJSON('/colors', (json) => {
            this.colors = json;
        })
    }

    // Starts the application (setup elements after the user logged in)
    // Callable if the 'account' property has been set
    setup() {
        // get the postits
        $.ajax({
            method: 'GET',
            url: `/postit?token=${this.account.token}`,
            data: 'json',
            async: false, // synchronous retrieving
            success: (json) => {
                let error = this.checkError(json);
                if (error !== false) {
                    console.error(error); // TODO Error
                } else {
                    $.each(json, (i, object) => {
                        // Adds the postit as first in the array 
                        this.postits.splice(0, 0, new Postit(
                            object.id,
                            this.colors.indexOf(object.color),
                            object.content,
                            object.relative_x,
                            object.relative_y
                        ));
                    })
                }
            },
            error: (xhr, status, error) => console.error(error)
        });
    
        this.updateIndexes();

        // Shows postits
        $.each(this.postits, (i, postit) => {
            postit.render();
            postit.show();
        });

        // Finally show things:
        this.login.element.fadeOut(200, () => {
            this.element.find('#add:first').click($.proxy(this.newPostit, this));
            this.element.find('#disconnect:first').click($.proxy(this.account.disconnect, this.account));
            this.element.css('display', 'flex');
        });
    }

    // Updates z-index according to postits order in the table
    // This aims to overlay postits correctly
    updateIndexes() {
        $.each(this.postits, (i, postit) => {
            postit.zIndex(i);
        });
    }

    // Creates a new postit
    newPostit() {
        let postit;

        $.ajax({
            method: 'POST',
            url: `/postit?token=${this.account.token}`,
            data: 'json',
            async: false,
            success: (json) => {
                let error = this.checkError(json);
                if (error !== false) {
                    console.error(error); // TODO Error
                } else {
                    postit = new Postit(json.id, 0, '', undefined, undefined);
                    postit.top = parseInt(postit.element.css('top'), 10)
                    postit.left = parseInt(postit.element.css('left'), 10)

                    this.postits.push(postit) // Adds the postit as first element (last in the array)
                }
            },
            error: (xhr, status, error) => console.error(error)
        })

        this.updateIndexes();

        postit.render();
        postit.show();
    }

    // Returns the corresponding error message if the message contains an error
    // Else returns false
    checkError(message) {
        if (message.hasOwnProperty('error_code')) {
            let entry = $.grep(this.errors, (error) => (error.code === message.error_code));
            return entry.length > 0 ? entry[0].message : false;
        } else return false;
    }
}