package br.com.jorgeacetozi.ebookChat.chatroom.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.jorgeacetozi.ebookChat.chatroom.domain.model.ChatRoom;
import br.com.jorgeacetozi.ebookChat.chatroom.domain.service.ChatRoomService;

/**
 * REST API for chat rooms (list and get by id) for Next.js / SPA clients.
 */
@RestController
@RequestMapping(value = "/api/chatrooms", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatRoomsApiController {

    private final ChatRoomService chatRoomService;

    public ChatRoomsApiController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @GetMapping
    public List<ChatRoom> list() {
        return chatRoomService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatRoom> getById(@PathVariable String id) {
        ChatRoom room = chatRoomService.findById(id);
        if (room == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(room, HttpStatus.OK);
    }
}
