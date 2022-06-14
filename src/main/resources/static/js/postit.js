// Not displayed by default
const POSTIT = `
    <div class="postit" class="hidden">
        <div class="header">
            <div class="color"><i class="fas fa-tint"></i></div>
            <div class="delete"><i class="fas fa-times"></i></div>
        </div>
        <textarea spellcheck="false" class="content"></textarea>
    </div>
`;

class Postit {
    constructor(id, colorI, content, left, top) {
        this.element = this.createElement();
        this.id = id;
        this.colorI = colorI; // index of the color the the application colors array
        this.content = content;

        this.left = left; // Offset from left
        this.top = top; // Offset from top

        this.element.css('left', left);
        this.element.css('top', top);

        // When the post-it is selected, place it on the foreground
        this.element.mousedown($.proxy(this.onSelect, this));

        this.element.find('.color:first').click($.proxy(this.onColorChange, this));
        this.element.find('.delete:first').click($.proxy(this.delete, this));

        // On written text changes, change the postit value
        this.element.find('textarea:first').change($.proxy(this.onTextChange, this));
    }

    // creates, append to the DOM and returns a new postit element
    createElement() {
        let element = $(POSTIT);
        element.appendTo($('#postit-container')).draggable({
            containment: "parent",
            grid: [10, 10],
            stop: $.proxy(this.onMove, this)
        });

        return element;
    }

    // https://api.jqueryui.com/draggable/
    // triggered when the postit is moved
    onMove(event, ui) {
        let offset = this.element.offset();
        this.left = offset.left;
        this.top = offset.top;

        this.toForeground()

        this.update();
    }

    // Triggered when the text changes
    onTextChange() {
        this.content = this.element.find('textarea:first').val();
        this.update();
    }

    // Shifts the color by 1 cell of the colors array
    onColorChange() {
        this.colorI = this.colorI + 1 >= app.colors.length ? 0 : this.colorI + 1;

        this.update();
        this.render();
    }

    // When the post-it is selected (mousedown)
    onSelect() {
        this.toForeground();
        this.update();
    }

    // Place the post-it in the foreground
    toForeground() {
        app.postits.splice(app.postits.indexOf(this), 1); // Removes the postit from the list
        app.postits.push(this); // Puts it at the top

        app.updateIndexes(); // Updates the indexes
    }

    // sets z-index of element
    zIndex(i) {
        this.element.css('z-index', i);
    }

    // Modifies the element using the stored values
    render() {
        this.element.css('background-color', '#' + app.colors[this.colorI]);
        this.element.find('textarea:first').val(this.content);
    }
    
    // Hides the post-it
    hide() {
        this.element.removeClass('showed');
        this.element.addClass('hidden');
    }

    // Shows the post-it
    show() {
        this.element.removeClass('hidden');
        this.element.addClass('showed');
    }

    // returns a server-valid json representation
    toJson() {
        return {
            id: this.id,
            color: app.colors[this.colorI],
            content: this.content,
            relative_x: this.left,
            relative_y: this.top
        };
    }

    // update data in database
    update() {
        $.ajax({
            method: 'PUT',
            url: `/postit?token=${app.account.token}`,
            contentType: 'application/json',
            data: JSON.stringify(this.toJson()),
            dataType: 'json',
            success: (json) => {
                let error = app.checkError(json);
                if (error !== false) {
                    console.error(error); // TODO Error
                }
            },
            error: (xhr, status, error) => console.error(error)
        })
    }

    // Delete this post-it
    delete() {
        $.ajax({
            method: 'DELETE',
            url: `/postit?token=${app.account.token}`,
            contentType: 'application/json',
            data: JSON.stringify({
                id: this.id
            }),
            dataType: 'json',
            success: (json) => {
                let error = app.checkError(json);
                if (error !== false) {
                    console.error(error); // TODO Error
                } else {
                    this.element.remove();
                    app.postits.splice(app.postits.indexOf(this), 0)
                }
            },
            error: (xhr, status, error) => console.error(error)
        });
    }
}