package com.golubets.ftp

import java.awt.Component
import java.awt.Container
import java.awt.FocusTraversalPolicy

class FocusTraversalOnArray
(private val m_Components: Array<Component>) : FocusTraversalPolicy() {
    private fun indexCycle(index: Int, delta: Int): Int {
        val size = m_Components.size
        return (index + delta + size) % size
    }

    private fun cycle(currentComponent: Component, delta: Int): Component {
        var index = -1
        loop@ for (i in m_Components.indices) {
            val component = m_Components[i]
            var c: Component? = currentComponent
            while (c != null) {
                if (component === c) {
                    index = i
                    break@loop
                }
                c = c.parent
            }
        }

        val initialIndex = index
        while (true) {
            val newIndex = indexCycle(index, delta)
            if (newIndex == initialIndex) {
                break
            }
            index = newIndex

            val component = m_Components[newIndex]
            if (component.isEnabled && component.isVisible && component.isFocusable) {
                return component
            }
        }
        return currentComponent
    }

    override fun getComponentAfter(container: Container, component: Component): Component {
        return cycle(component, 1)
    }

    override fun getComponentBefore(container: Container, component: Component): Component {
        return cycle(component, -1)
    }

    override fun getFirstComponent(container: Container): Component {
        return m_Components[0]
    }

    override fun getLastComponent(container: Container): Component {
        return m_Components[m_Components.size - 1]
    }

    override fun getDefaultComponent(container: Container): Component {
        return getFirstComponent(container)
    }
}