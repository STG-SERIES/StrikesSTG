rootProject.name = "StrikesSTG"

include("26.1")
include("26.1.2")
include("26.2")
include("26.3")

project(":26.1").projectDir = file("versions/26.1")
project(":26.1.2").projectDir = file("versions/26.1.2")
project(":26.2").projectDir = file("versions/26.2")
project(":26.3").projectDir = file("versions/26.3")
