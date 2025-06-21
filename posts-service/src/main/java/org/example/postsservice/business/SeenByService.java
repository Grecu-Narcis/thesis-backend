package org.example.postsservice.business;

import org.example.postsservice.models.seen_by.SeenBy;
import org.example.postsservice.repositories.SeenByRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeenByService {
    private final SeenByRepository seenByRepository;

    @Autowired
    public SeenByService(SeenByRepository seenByRepository) {
        this.seenByRepository = seenByRepository;
    }

    @Transactional
    public void markPostsAsSeen(String username, List<Long> postIds) {
        List<SeenBy> seenEntries = postIds.stream()
                .map(postId -> new SeenBy(username, postId))
                .toList();

        seenByRepository.saveAll(seenEntries);
    }
}
