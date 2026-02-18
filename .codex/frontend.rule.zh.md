# Refinex Frontend AI Development Rules

技术栈：

React
Radix UI
TailwindCSS

---

# UI 规则

必须优先使用：

Radix UI

禁止：

Ant Design
Element UI

---

# 图标规则

必须使用：

React Icons

---

# API 调用规则

必须在：

```
services/api.ts
```

禁止组件直接调用 fetch。

---

# 组件规则

结构：

```
components
pages
services
hooks
types
```

---

# 样式规则

必须使用 TailwindCSS。

禁止使用 CSS 文件。
