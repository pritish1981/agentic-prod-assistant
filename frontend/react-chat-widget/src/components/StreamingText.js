import React, { useEffect, useRef } from "react";


const StreamingText = ({ text }) => {
const ref = useRef();


useEffect(() => {
ref.current?.scrollIntoView({ behavior: "smooth" });
}, [text]);


return (
<div style={{ display: "flex", marginBottom: "10px" }}>
<div
style={{
background: "#e5e5ea",
padding: "10px 14px",
borderRadius: "12px",
maxWidth: "75%",
}}
ref={ref}
>
{text}
</div>
</div>
);
};


export default StreamingText;