import os
import requests
import json

# Per-mod: Update this for each mod!!!


MOD_ID = "tectonic"
MOD_VERSION = "3.0.27"
CHANGELOG = """
- Fix crash issues on Neoforge 26.2.
"""
UPLOAD_VERSIONS = [
    #("fabric", "21.1"),
    #("neoforge", "21.1"),
    #("fabric", "26.1"),
    #("neoforge", "26.1"),
    #("fabric", "26.2"),
    ("neoforge", "26.2"),
]

DEPENDENCIES = [
    {
        "name": "Lithostitched",
        "project_id": "XaDC71GB",
        "dependency_type": "required",
        "modId": 936015,
        "relationType": 3,
    }
]

MODRINTH_ID = "lWDHr9jE"
CURSEFORGE_ID = "686836"

RELEASE_TYPE = "release"

# Global: Should usually not be touched!

BASE_FOLDER = os.path.dirname(os.path.abspath(__file__))


MODRINTH_TOKEN = os.getenv('TOKEN_MR')
if not MODRINTH_TOKEN:
    raise EnvironmentError("MODRINTH_TOKEN is unset!")
MODRINTH_GAME_VERSIONS = {
    "21.1": ["1.21.1"],
    "26.1": ["26.1", "26.1.1", "26.1.2"],
    "26.2": ["26.2"],
}

CURSEFORGE_TOKEN = os.getenv('TOKEN_CF')
if not CURSEFORGE_TOKEN:
    raise EnvironmentError("CURSEFORGE_TOKEN is unset!")
CURSEFORGE_URL = f"https://minecraft.curseforge.com/api/v1/projects/{CURSEFORGE_ID}/upload-file"
CURSEFORGE_GAME_VERSIONS = {
    "21.1": [11779],
    "26.1": [15933, 16021, 16082],
    "26.2": [16498],
}
CURSEFORGE_LOADERS = {
    "fabric": 7499,
    "forge": 7498,
    "neoforge": 10150,
}


# Code

def upload_modrinth(loader: str, version: str, file_path: str, dependencies):

    game_versions = MODRINTH_GAME_VERSIONS.get(version)

    metadata = {
        "name": f"v{MOD_VERSION} ~ {loader.title()} {version}",
        "version_number": f"{MOD_VERSION}-{loader}-{version}",
        "project_id": MODRINTH_ID,
        "game_versions": game_versions,
        "loaders": [loader],
        "featured": True,
        "changelog": CHANGELOG,
        "version_type": RELEASE_TYPE,
        "file_parts": ["file"],
        "dependencies": dependencies
    }

    with open(file_path, 'rb') as mod_file:
        response = requests.post(
            "https://api.modrinth.com/v2/version",
            headers={
                "Authorization": MODRINTH_TOKEN
            },
            files={
                'file': (os.path.basename(file_path), mod_file, 'application/java-archive'),
            },
            data={
                'data': json.dumps(metadata)
            }
        )

        name = f"MR {loader.title()} {version}: "

        if response.status_code == 200:
            print(name + "Success")
            print(response.json())
        else:
            print(name + f"Failed ({response.status_code})")
            print(response.text)


def upload_curseforge(loader: str, version: str, file_path: str, dependencies):
    headers = {
        "X-Api-Token": CURSEFORGE_TOKEN
    }

    # Lookup version and loader IDs
    game_version_ids = CURSEFORGE_GAME_VERSIONS.get(version)
    modloader_id = CURSEFORGE_LOADERS.get(loader)

    if not game_version_ids or not modloader_id:
        print(f"Skipping CurseForge upload for {loader} {version}: unknown IDs")
        return

    # Metadata
    metadata = {
        "displayName": f"v{MOD_VERSION} ~ {loader.title()} {version}",
        "gameVersionNames": ["Server", "Client"],
        "gameVersions": game_version_ids + [modloader_id],
        "releaseType": RELEASE_TYPE,
        "changelog": CHANGELOG,
        "changelogType": "markdown",
        "dependencies": dependencies
    }
    metastr = json.dumps(metadata)

    with open(file_path, "rb") as mod_file:
        files = {
            "file": (os.path.basename(file_path), mod_file, "application/java-archive")
        }

        response = requests.post(
            f"https://minecraft.curseforge.com/api/projects/{CURSEFORGE_ID}/upload-file",
            headers=headers,
            files=files,
            data={"metadata": metastr},
            auth=("Apollo", CURSEFORGE_TOKEN)
        )

        name = f"CF {loader.title()} {version}: "

        if response.status_code == 200:
            print(name + "Success")
            print(response.json())
        else:
            print(name + f"Failed ({response.status_code})")
            print(response.text)


for modloader, game_version in UPLOAD_VERSIONS:
    mod_path = os.path.join(
        BASE_FOLDER,
        'versions',
        f'{game_version}-{modloader}',
        'build',
        'libs',
        f'{MOD_ID}-{MOD_VERSION}-{modloader}-{game_version}.jar'
    )

    dependencies = DEPENDENCIES.copy()
    if (modloader == "fabric"):
        dependencies.append(
            {
                "mod_name": "Fabric API",
                "modId": 306612,
                "relationType": 3,
                "project_id": "P7dR8mSH",
                "dependency_type": "required"
            }
        )

    if not os.path.exists(mod_path):
        print(f"File not found, skipping: {mod_path}")
        continue

    upload_modrinth(modloader, game_version, mod_path, dependencies)
    upload_curseforge(modloader, game_version, mod_path, dependencies)

input("Press any key to close")