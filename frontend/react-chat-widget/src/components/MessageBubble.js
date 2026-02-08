import React from "react";


const MessageBubble = ({ role, content }) => {
const isUser = role === "user";


return (
<div
style={{
display: "flex",
justifyContent: isUser ? "flex-end" : "flex-start",
marginBottom: "10px",
}}
>
<div
style={{
background: isUser ? "#007bff" : "#e5e5ea",
color: isUser ? "white" : "black",
padding: "10px 14px",
borderRadius: "12px",
maxWidth: "75%",
}}
>
{content}
</div>
</div>
);
};


export default MessageBubble;