import React, { useMemo, useState } from "react";
import MessageBubble from "./MessageBubble";
import StreamingText from "./StreamingText";
import { streamChat } from "../api/chatApi";

const ChatWidget = () => {
const [messages, setMessages] = useState([]);
const [input, setInput] = useState("");
const [streamingMsg, setStreamingMsg] = useState("");
const [isStreaming, setIsStreaming] = useState(false);
const sessionId = useMemo(() => {
	return `session-${Date.now()}-${Math.random().toString(36).slice(2)}`;
}, []);
const userId = "demo-user";


const sendMessage = async () => {
if (!input.trim()) return;


const userMsg = { role: "user", content: input };
setMessages((prev) => [...prev, userMsg]);
setInput("");
setStreamingMsg("");
setIsStreaming(true);

let fullResponse = "";


await streamChat(
{ sessionId, userId, message: input },
(chunk) => {
fullResponse += chunk;
setStreamingMsg((prev) => prev + chunk);
},
() => {
setMessages((prev) => [
...prev,
{ role: "assistant", content: fullResponse },
]);
setStreamingMsg("");
setIsStreaming(false);
}
);
};


return (
<div style={styles.container}>
<div style={styles.header}>Enterprise Production AI Agent</div>


<div style={styles.chatArea}>
{messages.map((msg, idx) => (
<MessageBubble key={idx} role={msg.role} content={msg.content} />
))}


{isStreaming && (
<StreamingText text={streamingMsg} />
)}
</div>


<div style={styles.inputArea}>
<input
style={styles.input}
value={input}
placeholder="Ask about production incidents..."
onChange={(e) => setInput(e.target.value)}
onKeyDown={(e) => e.key === "Enter" && sendMessage()}
/>
<button style={styles.button} onClick={sendMessage}>
Send
</button>
</div>
</div>
);
};


const styles = {
container: {
width: "420px",
height: "600px",
borderRadius: "16px",
boxShadow: "0 10px 30px rgba(0,0,0,0.15)",
display: "flex",
flexDirection: "column",
fontFamily: "Arial",
overflow: "hidden",
margin: "40px auto",
},
header: {
padding: "16px",
fontWeight: "bold",
borderBottom: "1px solid #eee",
},
chatArea: {
flex: 1,
padding: "12px",
overflowY: "auto",
background: "#fafafa",
},
inputArea: {
display: "flex",
borderTop: "1px solid #eee",
},
input: {
flex: 1,
padding: "12px",
border: "none",
outline: "none",
},
button: {
padding: "12px 18px",
border: "none",
cursor: "pointer",
},
};


export default ChatWidget;