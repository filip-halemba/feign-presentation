package pl.epoint.service

import org.springframework.web.bind.annotation.*
import pl.epoint.feign.Developer

/**
 * @author Filip Halemba
 */
@RestController
@RequestMapping("/dev")
class DeveloperRestApi {

    private val developers = mutableListOf(Developer(1, "Martin Fowler"), Developer(2, "Uncle Bob"))

    @GetMapping
    fun get(): List<Developer> = developers

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Int): Developer? = developers.find { it.id == id }

    @PostMapping(consumes = arrayOf("application/json"))
    fun post(@RequestBody developer: Developer): Developer = developer

}
