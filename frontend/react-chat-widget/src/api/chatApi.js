import axios from "axios";


const API_BASE = "http://localhost:8080/api/chat";


export const streamChat = async (payload, onChunk, onComplete) => {
const response = await fetch(API_BASE, {
method: "POST",
headers: {
"Content-Type": "application/json",
},
body: JSON.stringify(payload),
});

if (!response.ok) {
throw new Error(`Chat request failed with status ${response.status}`);
}

if (!response.body) {
throw new Error("Chat response body is empty");
}

const reader = response.body.getReader();
const decoder = new TextDecoder("utf-8");
let buffer = "";

let done = false;

while (!done) {
const result = await reader.read();
done = result.done;

buffer += decoder.decode(result.value || new Uint8Array(), { stream: !done });

let boundaryIndex = buffer.indexOf("\n\n");
while (boundaryIndex !== -1) {
const frame = buffer.slice(0, boundaryIndex);
buffer = buffer.slice(boundaryIndex + 2);

const lines = frame.split("\n");
for (const line of lines) {
if (line.startsWith("data:")) {
const data = line.slice(5).trim();
if (data) {
onChunk(data);
}
}
}

boundaryIndex = buffer.indexOf("\n\n");
}
}

const remaining = buffer.trim();
if (remaining.startsWith("data:")) {
const data = remaining.slice(5).trim();
if (data) {
onChunk(data);
}
}

onComplete();
};