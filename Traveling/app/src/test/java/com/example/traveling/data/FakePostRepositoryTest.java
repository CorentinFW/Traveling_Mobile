package com.example.traveling.data;

import com.example.traveling.model.Post;
import com.example.traveling.model.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FakePostRepositoryTest {

    private FakePostRepository repository;

    @Before
    public void setUp() {
        repository = FakePostRepository.getInstance();
        repository.resetForTests();
    }

    @Test
    public void feed_containsSeededPosts() {
        List<Post> feed = repository.getFeedPosts();
        assertTrue(feed.size() >= 10);
        assertEquals("p10", feed.get(0).getId());
    }

    @Test
    public void addPost_insertsAtTop() {
        User currentUser = new User("u_current", "travel.student", "Travel Student", null);

        Post created = repository.addPost(
                currentUser,
                "Lyon",
                "Vue depuis Fourviere",
                Arrays.asList("france", "city")
        );

        List<Post> feed = repository.getFeedPosts();
        assertEquals(created.getId(), feed.get(0).getId());
        assertEquals("Lyon", feed.get(0).getLocationName());
    }

    @Test
    public void searchPosts_matchesLocationAuthorAndTag() {
        assertTrue(!repository.searchPosts("kyoto").isEmpty());
        assertTrue(!repository.searchPosts("alice").isEmpty());
        assertTrue(!repository.searchPosts("market").isEmpty());
    }

    @Test
    public void getPostById_returnsExpectedPost() {
        Post post = repository.getPostById("p7");
        assertNotNull(post);
        assertEquals("Kyoto", post.getLocationName());
    }
}



