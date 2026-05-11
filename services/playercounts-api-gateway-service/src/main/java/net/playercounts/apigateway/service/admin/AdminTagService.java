package net.playercounts.apigateway.service.admin;

import net.playercounts.apigateway.dto.request.CreateTagRequest;
import net.playercounts.apigateway.dto.request.UpdateTagRequest;
import net.playercounts.apigateway.dto.response.TagResponse;
import net.playercounts.apigateway.repository.admin.TagRepository;
import net.playercounts.models.entity.ServerTag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminTagService {

    private final TagRepository tagRepository;

    public AdminTagService(
            TagRepository tagRepository
    ) {
        this.tagRepository = tagRepository;
    }

    public TagResponse createTag(
            CreateTagRequest request
    ) {

        tagRepository.findByNameIgnoreCase(
                request.name()
        ).ifPresent(existing -> {

            throw new IllegalStateException(
                    "Tag already exists"
            );
        });

        ServerTag tag = new ServerTag();

        tag.setName(request.name());
        tag.setColor(request.color());
        tag.setCreatedAt(System.currentTimeMillis());

        ServerTag savedTag =
                tagRepository.save(tag);

        return mapResponse(savedTag);
    }

    public List<TagResponse> getTags() {

        return tagRepository.findAll()
                .stream()
                .map(this::mapResponse)
                .toList();
    }

    public TagResponse getTag(
            Long id
    ) {
        ServerTag tag = getTagOrThrow(id);

        return mapResponse(tag);
    }

    public TagResponse updateTag(
            Long id,
            UpdateTagRequest request
    ) {

        ServerTag tag = getTagOrThrow(id);

        tag.setName(request.name());
        tag.setColor(request.color());

        ServerTag updatedTag = tagRepository.save(tag);

        return mapResponse(updatedTag);
    }

    public void deleteTag(
            Long id
    ) {

        if (!tagRepository.existsById(id)) {

            throw new IllegalStateException(
                    "Tag not found"
            );
        }

        tagRepository.deleteById(id);
    }

    private ServerTag getTagOrThrow(
            Long id
    ) {

        return tagRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Tag not found"
                        )
                );
    }

    private TagResponse mapResponse(
            ServerTag tag
    ) {

        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getColor(),
                tag.getCreatedAt()
        );
    }
}