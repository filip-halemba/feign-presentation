package pl.epoint

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * @author Filip Halemba
 */
@SpringBootApplication
open class FeignPresentation

fun main(args: Array<String>) {
    SpringApplication.run(FeignPresentation::class.java, *args)
}

