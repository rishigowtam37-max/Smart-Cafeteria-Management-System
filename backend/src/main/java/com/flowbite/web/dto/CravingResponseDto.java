package com.flowbite.web.dto;

import com.flowbite.service.CravingService.Suggestion;

import java.util.List;

/**
 * A Smart Craving answer on the wire.
 *
 * <p>Each pick carries the full menu item, so the browser can render a card from
 * it directly and does not have to join the suggestion back against the menu it
 * happens to be holding - which might already be out of date.
 */
public record CravingResponseDto(String headline, List<PickDto> picks) {

    /** One suggestion: a real menu item, and why the model chose it. */
    public record PickDto(FoodItemDto item, String reason) {
    }

    public static CravingResponseDto from(Suggestion suggestion) {
        List<PickDto> picks = suggestion.picks().stream()
                .map(pick -> new PickDto(FoodItemDto.from(pick.item()), pick.reason()))
                .toList();

        return new CravingResponseDto(suggestion.headline(), picks);
    }
}
