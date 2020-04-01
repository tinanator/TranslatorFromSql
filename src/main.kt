
fun main(args : Array<String>) {
    if (args.isEmpty()) {
        println("error")
    }
    val translator : Traslator = Traslator()
    for (a in args) {
        println(a)
    }
    val mongodbcommand = translator.translate(args)
    print(mongodbcommand)
}

