package com.flowbite.web;

import com.flowbite.service.CravingService;
import com.flowbite.web.dto.CravingResponseDto;
import com.flowbite.web.dto.Requests.CravingRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Smart Craving.
 *
 * <p>Customer-only, like the cart - {@link RoleInterceptor} guards the path.
 * Worth guarding for a practical reason as well as a tidy one: every call here
 * costs a Gemini request, so it should not be reachable without a session.
 */
@RestController
@RequestMapping("/api/craving")
public class CravingController {

    private final CravingService cravingService;

    public CravingController(CravingService cravingService) {
        this.cravingService = cravingService;
    }

    /**
     * Suggests menu items for a craving.
     *
     * @return a headline and up to three picks, each a real in-stock menu item.
     */
    @PostMapping
    public CravingResponseDto suggest(@Valid @RequestBody CravingRequest request) {
        return CravingResponseDto.from(cravingService.suggest(request.craving()));
    }
}
