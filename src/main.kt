
fun main(args : Array<String>) {
    if (args.isEmpty()) {
        println("error")
    }
    val translator : Traslator = Traslator()
    val mongodbcommand = translator.translate(args)
    for (word in mongodbcommand) {
        print("$word ")
    }
}

