package org.develnext.jphp.swing.classes.components.support;


import org.develnext.jphp.swing.XYLayout;
import php.runtime.Memory;
import php.runtime.env.Environment;
import org.develnext.jphp.swing.ComponentProperties;
import org.develnext.jphp.swing.SwingExtension;
import php.runtime.memory.ArrayMemory;
import php.runtime.memory.LongMemory;
import php.runtime.memory.ObjectMemory;
import php.runtime.reflection.ClassEntity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static php.runtime.annotation.Reflection.*;

@Name(SwingExtension.NAMESPACE + "UIContainer")
abstract public class UIContainer extends UIElement {

    public UIContainer(Environment env) {
        super(env);
    }

    public UIContainer(Environment env, ClassEntity clazz) {
        super(env, clazz);
    }

    protected Container getComponentsContainer() {
        return getContainer();
    }
    abstract public Container getContainer();

    @Override
    public Component getComponent() {
        return getContainer();
    }

    @Signature(
            @Arg(value = "component", typeClass = SwingExtension.NAMESPACE + "UIElement")
    )
    public Memory remove(Environment env, Memory... args) {
        UIElement element = unwrap(args[0]);
        getComponentsContainer().remove(element.getComponent());
        return Memory.NULL;
    }

    @Signature(@Arg("index"))
    public Memory removeByIndex(Environment env, Memory... args) {
        getComponentsContainer().remove(args[0].toInteger());
        return Memory.NULL;
    }

    @Signature
    public Memory removeAll(Environment env, Memory... args) {
        getComponentsContainer().removeAll();
        return Memory.NULL;
    }

    @Signature(@Arg("index"))
    public Memory getComponent(Environment env, Memory... args) {
        return new ObjectMemory(UIElement.of(
                env, getComponentsContainer().getComponent(args[0].toInteger())
        ));
    }

    @Signature
    public Memory getComponentCount(Environment env, Memory... args) {
        return LongMemory.valueOf(getComponentsContainer().getComponentCount());
    }

    @Signature
    public Memory getComponents(Environment env, Memory... args) {
        ArrayMemory r = new ArrayMemory();
        Container container = getComponentsContainer();
        for(int i = 0; i < container.getComponentCount(); i++) {
            r.add(UIElement.of(env, container.getComponent(i)));
        }
        return r.toConstant();
    }

    @Signature({
            @Arg(value = "component", typeClass = SwingExtension.NAMESPACE + "UIElement"),
            @Arg(value = "index", optional = @Optional("NULL")),
            @Arg(value = "constraints", optional = @Optional("NULL"))
    })
    public Memory add(Environment env, Memory... args){
        UIElement element = unwrap(args[0]);

        if (args[1].isNull()) {
            if (args[2].isNull())
                getContainer().add(element.getComponent());
            else
                getContainer().add(element.getComponent(), args[2].toString());
        } else {
            if (args[2].isNull())
                getContainer().add(element.getComponent(), args[1].toInteger());
            else
                getContainer().add(element.getComponent(), args[2].toString(), args[1].toInteger());
        }

        return args[0];
    }


    @Signature(@Arg("type"))
    public Memory setLayout(Environment env, Memory... args) {
        String s = args[0].toString().toLowerCase();

        Container component = getComponentsContainer();
        if ("absolute".equals(s))
            component.setLayout(new XYLayout());
        else if ("grid".equals(s)) {
            component.setLayout(new GridLayout());
        } else if ("flow".equals(s))
            component.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        else if ("grid-bag".equals(s))
            component.setLayout(new GridBagLayout());
        else if ("border".equals(s))
            component.setLayout(new BorderLayout());
        else if ("card".equals(s))
            component.setLayout(new CardLayout());

        return null;
    }

    protected Component getComponent(Container where, String group){
        if (where instanceof JFrame)
            where = ((JFrame) where).getRootPane();

        int count = where instanceof JMenu ? ((JMenu) where).getItemCount() : where.getComponentCount();
        for(int i = 0; i < count; i++){
            Component el = where instanceof JMenu ? ((JMenu) where).getItem(i) : where.getComponent(i);
            if (el == null)
                continue;

            ComponentProperties properties = SwingExtension.getProperties(el);

            if (properties != null && properties.hasGroup(group)){
                return el;
            }
            if (el instanceof Container){
                Component r = getComponent((Container)el, group);
                if (r != null)
                    return r;
            }
        }
        return null;
    }

    protected java.util.List<Component> getComponents(Container where, String group){
        java.util.List<Component> result = new ArrayList<Component>();

        if (where instanceof JFrame)
            where = ((JFrame) where).getRootPane();

        int count = where instanceof JMenu ? ((JMenu) where).getItemCount() : where.getComponentCount();
        for(int i = 0; i < count; i++){
            Component el = where instanceof JMenu ? ((JMenu) where).getItem(i) : where.getComponent(i);
            if (el == null)
                continue;

            ComponentProperties properties = SwingExtension.getProperties(el);

            if (properties != null && properties.hasGroup(group)){
                result.add(el);
            }

            if (el instanceof Container){
                result.addAll(getComponents((Container)el, group));
            }
        }
        return result;
    }

    @Signature(@Arg("group"))
    public Memory getComponentByGroup(Environment env, Memory... args){
        Component component = null;
        String name = args[0].toString();

        Container container = getContainer();
        component = getComponent(container, name);

        if (component == null)
            return Memory.NULL;

        return new ObjectMemory(UIElement.of(env, component));
    }

    @Signature(@Arg("group"))
    public Memory getComponentsByGroup(Environment env, Memory... args){
        java.util.List<Component> components = getComponents(getContainer(), args[0].toString());
        ArrayMemory result = new ArrayMemory();
        for(Component el : components)
            result.add(new ObjectMemory(UIElement.of(env, el)));

        return result.toConstant();
    }
}
