import { BrowserRouter } from "react-router-dom";
import ChatMindLayout from "./components/ChatMindLayout.tsx";
import { ChatSessionsProvider } from "./contexts/ChatSessionsContext.tsx";

function App() {
  return (
    <BrowserRouter>
      <ChatSessionsProvider>
        <ChatMindLayout />
      </ChatSessionsProvider>
    </BrowserRouter>
  );
}

export default App;
