package com.example.bot.spring;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

@SpringComponent
@UIScope
public class MenuEditor extends VerticalLayout implements KeyNotifier {

    private final MenuRepository repository;

    private Menu menu;

    TextField name = new TextField("Name");
    TextField menuId = new TextField("Menu ID");

    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    Binder<Menu> binder = new Binder<>(Menu.class);
    private ChangeHandler changeHandler;

    @Autowired
    public MenuEditor(MenuRepository repository) {
        this.repository = repository;

        add(name, menuId, actions);

        binder.bindInstanceFields(this);

        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> save());

        save.addClickListener(e -> save());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> editMenu(menu));
        setVisible(false);
    }

    void delete() {
        repository.delete(menu);
        changeHandler.onChange();
    }

    void save() {
        repository.save(menu);
        changeHandler.onChange();
    }

    public interface ChangeHandler {
        void onChange();
    }

    public final void editMenu(Menu c) {
        if (c == null) {
            setVisible(false);
            return;
        }
        final boolean persisted = c.getId() != null;
        if (persisted) {
            menu = repository.findById(c.getId()).get();
        } else {
            menu = c;
        }

        cancel.setVisible(persisted);
        binder.setBean(menu);
        setVisible(true);
        name.focus();
    }

    public void setChangeHandler(ChangeHandler h) {
        changeHandler = h;
    }
}
