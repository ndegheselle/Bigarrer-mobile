<script setup lang="ts">
import { ref, onMounted } from 'vue';
import * as THREE from 'three';
import * as LocAR from 'locar';

const canvas = ref<HTMLCanvasElement>();
const gpsPosition = ref<GeolocationPosition>();
const cubePosition = ref<GeolocationPosition>();
function createOrientationControl(camera: THREE.Camera): LocAR.DeviceOrientationControls {
    const deviceOrientationControls = new LocAR.DeviceOrientationControls(camera);

    deviceOrientationControls.on("deviceorientationgranted", ev => {
        ev.target.connect();
    });

    deviceOrientationControls.on("deviceorientationerror", error => {
        alert(`Device orientation error: code ${error.code} message ${error.message}`);
    });

    deviceOrientationControls.init();
    return deviceOrientationControls;
}

onMounted(() => {
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.001, 100);
    const renderer = new THREE.WebGLRenderer({ canvas: canvas.value });
    renderer.setSize(window.innerWidth, window.innerHeight);

    window.addEventListener("resize", () => {
        renderer.setSize(window.innerWidth, window.innerHeight);
        camera.aspect = window.innerWidth / window.innerHeight;
        camera.updateProjectionMatrix();
    });

    const locar = new LocAR.LocationBased(scene, camera);
    const cam = new LocAR.Webcam({ video: { facingMode: "environment" } });

    cam.on("webcamstarted", (ev: any) => {
        scene.background = ev.texture;
    });

    cam.on("webcamerror", (error: any) => {
        alert(`Webcam error: code ${error.code} message ${error.message}`);
    });

    const orientation = createOrientationControl(camera);

    locar.startGps();
    locar.on("gpsupdate", (ev: any) => {
        gpsPosition.value = ev.position;
    });

    function onPlaceCube() {
        if (!gpsPosition.value) return;
        const { longitude, latitude, altitude } = gpsPosition.value.coords;
        const mesh = new THREE.Mesh(
            new THREE.BoxGeometry(2, 2, 2),
            new THREE.MeshBasicMaterial({ color: 0xff0000 })
        );
        cubePosition.value = gpsPosition.value;
        locar.add(mesh, longitude, latitude, altitude ?? 0);
    }

    canvas.value!.addEventListener("click", onPlaceCube);

    renderer.setAnimationLoop(() => {
        orientation.update();
        renderer.render(scene, camera);
    });
});
</script>

<template>
    <canvas ref="canvas" />
    <div class="absolute flex flex-col">
        <span>Current position: {{ gpsPosition?.coords.longitude }}, {{ gpsPosition?.coords.latitude }}, {{ gpsPosition?.coords.altitude }}</span>
        <span class="text-primary">Cube position: {{ cubePosition?.coords.longitude }}, {{ cubePosition?.coords.latitude }}, {{ cubePosition?.coords.altitude }}</span>
    </div>
</template>