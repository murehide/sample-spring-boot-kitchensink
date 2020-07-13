package com.example.bot.spring;

import org.springframework.util.StringUtils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends VerticalLayout {

    private final MenuRepository menuRepository;

    private final MenuEditor editor;

    final Grid<Menu> grid;

    final TextField filter;

    private final Button addNewBtn;

    public MainView(MenuRepository repo, MenuEditor editor) {
        this.menuRepository = repo;
        this.editor = editor;
        this.grid = new Grid<>(Menu.class);
        this.filter = new TextField();
        this.addNewBtn = new Button("New menu", VaadinIcon.PLUS.create());

        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
        add(actions, grid, editor);

        grid.setHeight("200px");
        grid.setColumns("id", "Name", "Menu ID");
        grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);

        filter.setPlaceholder("Filter by menu ID");

        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listMenus(e.getValue()));

        grid.asSingleSelect().addValueChangeListener(e -> {
            editor.editMenu(e.getValue());
        });

        addNewBtn.addClickListener(e -> editor.editMenu(new Menu("", "")));

        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listMenus(filter.getValue());
        });

        listMenus(null);
    }

    void listMenus(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(menuRepository.findAll());
        } else {
            grid.setItems(menuRepository.findByMenuId(filterText));
        }
    }
}
