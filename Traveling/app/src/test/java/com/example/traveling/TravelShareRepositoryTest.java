package com.example.traveling;

import com.example.traveling.travelshare.data.InMemoryTravelShareMessageRepository;
import com.example.traveling.travelshare.data.InMemoryTravelSharePostRepository;
import com.example.traveling.travelshare.data.InMemoryTravelShareSessionRepository;
import com.example.traveling.travelshare.data.InMemoryTravelShareUserRepository;
import com.example.traveling.travelshare.domain.TravelShareConversation;
import com.example.traveling.travelshare.domain.TravelShareGroup;
import com.example.traveling.travelshare.domain.TravelShareMessage;
import com.example.traveling.travelshare.domain.TravelSharePost;
import com.example.traveling.travelshare.domain.TravelShareUser;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TravelShareRepositoryTest {

    @Test
    public void searchReturnsResultsForMatchingLocation() {
        InMemoryTravelSharePostRepository repository = new InMemoryTravelSharePostRepository();

        List<TravelSharePost> results = repository.searchPosts("Tokyo");

        assertFalse(results.isEmpty());
        assertEquals("Tokyo", results.get(0).getLocationName());
    }

    @Test
    public void createPostAddsItemAtTopOfFeed() {
        InMemoryTravelSharePostRepository repository = new InMemoryTravelSharePostRepository();

        TravelSharePost created = repository.createPost(
                "Corentin",
                "Paris",
                "Sunset sur la Seine",
                "Printemps 2026",
                "Metro ligne 1"
        );

        List<TravelSharePost> feed = repository.getFeedPosts();
        assertEquals(created.getId(), feed.get(0).getId());
        assertEquals("Corentin", feed.get(0).getAuthorName());

        List<TravelSharePost> search = repository.searchPosts("Seine");
        assertFalse(search.isEmpty());
        assertEquals(created.getId(), search.get(0).getId());
    }

    @Test
    public void seedContainsFiveUsersAndThreeFriendsForCorentin() {
        InMemoryTravelShareUserRepository repository = new InMemoryTravelShareUserRepository();

        List<TravelShareUser> users = repository.getUsers();
        assertEquals(5, users.size());

        TravelShareUser corentin = repository.getUserByDisplayName("Corentin");
        assertNotNull(corentin);
        assertEquals(3, corentin.getFriendIds().size());
        assertEquals(3, repository.getFriendsOf("Corentin").size());
    }

    @Test
    public void seedContainsFifteenPostsAndThreePerUser() {
        InMemoryTravelSharePostRepository repository = new InMemoryTravelSharePostRepository();

        List<TravelSharePost> feed = repository.getFeedPosts();
        assertEquals(15, feed.size());
        assertEquals(3, repository.getPostsByAuthor("Corentin").size());
        assertEquals(3, repository.getPostsByAuthor("Lina").size());
        assertEquals(3, repository.getPostsByAuthor("Mehdi").size());
        assertEquals(3, repository.getPostsByAuthor("Camille").size());
        assertEquals(3, repository.getPostsByAuthor("Nora").size());
    }

    @Test
    public void messagesRepositoryReturnsSeededConversations() {
        InMemoryTravelShareMessageRepository repository = new InMemoryTravelShareMessageRepository();

        List<TravelShareConversation> conversations = repository.getConversations();

        assertEquals(3, conversations.size());
        assertEquals("c1", conversations.get(0).getId());
    }

    @Test
    public void sendMessageAddsMessageAndMovesConversationToTop() {
        InMemoryTravelShareMessageRepository repository = new InMemoryTravelShareMessageRepository();

        TravelShareMessage sent = repository.sendMessage("c3", "Corentin", "On prepare un groupe prive ?");

        assertNotNull(sent);
        List<TravelShareMessage> thread = repository.getMessages("c3");
        assertFalse(thread.isEmpty());
        assertEquals("On prepare un groupe prive ?", thread.get(thread.size() - 1).getText());

        List<TravelShareConversation> conversations = repository.getConversations();
        assertEquals("c3", conversations.get(0).getId());
        assertEquals("On prepare un groupe prive ?", conversations.get(0).getLastMessagePreview());
    }

    @Test
    public void sendMessageRejectsBlankText() {
        InMemoryTravelShareMessageRepository repository = new InMemoryTravelShareMessageRepository();

        TravelShareMessage sent = repository.sendMessage("c1", "Corentin", "   ");

        assertNull(sent);
    }

    @Test
    public void createGroupAddsOwnerAndMembers() {
        InMemoryTravelShareMessageRepository repository = new InMemoryTravelShareMessageRepository();

        TravelShareGroup created = repository.createGroup(
                "Week-end photo",
                "Corentin",
                java.util.Arrays.asList("Lina", "Mehdi", "Lina")
        );

        assertNotNull(created);
        assertEquals("Week-end photo", created.getName());
        assertTrue(created.getMemberNames().contains("Corentin"));
        assertTrue(created.getMemberNames().contains("Lina"));
        assertEquals(3, created.getMemberNames().size());
    }

    @Test
    public void addMemberDoesNotDuplicate() {
        InMemoryTravelShareMessageRepository repository = new InMemoryTravelShareMessageRepository();

        boolean firstAdd = repository.addMemberToGroup("g1", "Nora");
        boolean secondAdd = repository.addMemberToGroup("g1", "Nora");

        assertTrue(firstAdd);
        assertFalse(secondAdd);
        assertTrue(repository.getGroupById("g1").getMemberNames().contains("Nora"));
    }

    @Test
    public void sendGroupMessageAppendsToGroup() {
        InMemoryTravelShareMessageRepository repository = new InMemoryTravelShareMessageRepository();

        TravelShareMessage sent = repository.sendGroupMessage("g1", "Corentin", "Je peux reserver le train");

        assertNotNull(sent);
        assertEquals("g1", sent.getConversationId());
        assertEquals("Je peux reserver le train", repository.getGroupById("g1").getMessages().get(repository.getGroupById("g1").getMessages().size() - 1).getText());
    }

    @Test
    public void toggleLikeUpdatesCounterAndState() {
        InMemoryTravelSharePostRepository repository = new InMemoryTravelSharePostRepository();
        TravelSharePost post = repository.getPostById("1");

        assertNotNull(post);
        int initialLikeCount = post.getLikeCount();

        boolean liked = repository.toggleLike("1");

        assertTrue(liked);
        assertEquals(initialLikeCount + 1, post.getLikeCount());

        boolean unliked = repository.toggleLike("1");

        assertFalse(unliked);
        assertEquals(initialLikeCount, post.getLikeCount());
    }

    @Test
    public void sessionRepositorySwitchesAnonymousAndConnectedMode() {
        InMemoryTravelShareSessionRepository sessionRepository = new InMemoryTravelShareSessionRepository();

        assertFalse(sessionRepository.isAuthenticated());

        sessionRepository.login("Corentin");

        assertTrue(sessionRepository.isAuthenticated());
        assertEquals("Corentin", sessionRepository.getDisplayName());

        sessionRepository.logout();

        assertFalse(sessionRepository.isAuthenticated());
    }
}
