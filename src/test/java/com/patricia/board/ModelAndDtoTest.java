package com.patricia.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.patricia.board.dto.BoardResponse;
import com.patricia.board.dto.CreateBoardResponse;
import com.patricia.board.dto.CursorMessage;
import com.patricia.board.model.BoardState;
import com.patricia.board.model.Point;
import com.patricia.board.model.Stroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ModelAndDtoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testPoint() throws Exception {
        Point p1 = new Point();
        p1.setX(10.0);
        p1.setY(20.0);

        assertEquals(10.0, p1.getX());
        assertEquals(20.0, p1.getY());

        Point p2 = new Point(10.0, 20.0);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertEquals("Point(x=10.0, y=20.0)", p1.toString());

        Point p3 = new Point(15.0, 20.0);
        assertNotEquals(p1, p3);

        String json = objectMapper.writeValueAsString(p1);
        Point deserialized = objectMapper.readValue(json, Point.class);
        assertEquals(p1, deserialized);
    }

    @Test
    void testStroke() throws Exception {
        UUID strokeId = UUID.randomUUID();
        Instant now = Instant.now();
        List<Point> points = List.of(new Point(1.0, 2.0));

        Stroke s1 = new Stroke();
        s1.setId(strokeId);
        s1.setColor("#000000");
        s1.setWidth(3);
        s1.setPoints(points);
        s1.setCreatedAt(now);

        assertEquals(strokeId, s1.getId());
        assertEquals("#000000", s1.getColor());
        assertEquals(3, s1.getWidth());
        assertEquals(points, s1.getPoints());
        assertEquals(now, s1.getCreatedAt());

        Stroke s2 = new Stroke(strokeId, "#000000", 3, points, now);
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotNull(s1.toString());

        Stroke s3 = new Stroke(UUID.randomUUID(), "#FFFFFF", 4, points, now);
        assertNotEquals(s1, s3);

        String json = objectMapper.writeValueAsString(s1);
        Stroke deserialized = objectMapper.readValue(json, Stroke.class);
        assertEquals(s1, deserialized);
    }

    @Test
    void testBoardState() throws Exception {
        UUID boardId = UUID.randomUUID();
        List<Stroke> strokes = new ArrayList<>();
        strokes.add(new Stroke(UUID.randomUUID(), "#000", 2, List.of(new Point(0.0, 0.0)), Instant.now()));

        BoardState b1 = new BoardState();
        b1.setBoardId(boardId);
        b1.setStrokes(strokes);

        assertEquals(boardId, b1.getBoardId());
        assertEquals(strokes, b1.getStrokes());

        // lastActivityAt is set to now() by the convenience constructors (TTL
        // clock), so equality is only stable once it's aligned explicitly.
        BoardState b2 = new BoardState(boardId);
        b2.setStrokes(strokes);
        b2.setLastActivityAt(b1.getLastActivityAt());
        assertEquals(b1, b2);

        BoardState b3 = new BoardState(boardId, strokes);
        b3.setLastActivityAt(b1.getLastActivityAt());
        assertEquals(b1, b3);
        assertEquals(b1.hashCode(), b3.hashCode());
        assertNotNull(b1.toString());

        String json = objectMapper.writeValueAsString(b1);
        BoardState deserialized = objectMapper.readValue(json, BoardState.class);
        assertEquals(b1.getBoardId(), deserialized.getBoardId());
        assertEquals(b1.getStrokes().size(), deserialized.getStrokes().size());
    }

    @Test
    void testCreateBoardResponse() throws Exception {
        UUID boardId = UUID.randomUUID();
        CreateBoardResponse r1 = new CreateBoardResponse();
        r1.setBoardId(boardId);

        assertEquals(boardId, r1.getBoardId());

        CreateBoardResponse r2 = new CreateBoardResponse(boardId);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());

        String json = objectMapper.writeValueAsString(r1);
        CreateBoardResponse deserialized = objectMapper.readValue(json, CreateBoardResponse.class);
        assertEquals(r1, deserialized);
    }

    @Test
    void testBoardResponse() throws Exception {
        UUID boardId = UUID.randomUUID();
        List<Stroke> strokes = List.of(new Stroke(UUID.randomUUID(), "#111", 1, new ArrayList<>(), Instant.now()));

        BoardResponse r1 = new BoardResponse();
        r1.setBoardId(boardId);
        r1.setStrokes(strokes);

        assertEquals(boardId, r1.getBoardId());
        assertEquals(strokes, r1.getStrokes());

        BoardResponse r2 = new BoardResponse(boardId, strokes);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());

        String json = objectMapper.writeValueAsString(r1);
        BoardResponse deserialized = objectMapper.readValue(json, BoardResponse.class);
        assertEquals(r1.getBoardId(), deserialized.getBoardId());
        assertEquals(r1.getStrokes().size(), deserialized.getStrokes().size());
    }

    @Test
    void testCursorMessage() throws Exception {
        CursorMessage c1 = new CursorMessage();
        c1.setUserId("user-1");
        c1.setX(100.5);
        c1.setY(200.7);

        assertEquals("user-1", c1.getUserId());
        assertEquals(100.5, c1.getX());
        assertEquals(200.7, c1.getY());

        CursorMessage c2 = new CursorMessage("user-1", 100.5, 200.7);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotNull(c1.toString());

        String json = objectMapper.writeValueAsString(c1);
        CursorMessage deserialized = objectMapper.readValue(json, CursorMessage.class);
        assertEquals(c1, deserialized);
    }
}
