package com.bilderlings.back

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<BackApplication>().with(TestcontainersConfiguration::class).run(*args)
}
