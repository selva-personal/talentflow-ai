import { Canvas, useFrame } from '@react-three/fiber'
import { Float, MeshDistortMaterial, Sphere, Stars } from '@react-three/drei'
import { useRef, useMemo } from 'react'
import type { Mesh } from 'three'
import * as THREE from 'three'

function AiOrb() {
  const ref = useRef<Mesh>(null)
  useFrame((state) => {
    if (ref.current) {
      ref.current.rotation.y = state.clock.elapsedTime * 0.25
      ref.current.rotation.x = Math.sin(state.clock.elapsedTime * 0.2) * 0.15
    }
  })
  return (
    <Float speed={2} rotationIntensity={0.4} floatIntensity={1.2}>
      <Sphere ref={ref} args={[1.2, 64, 64]}>
        <MeshDistortMaterial
          color="#7c3aed"
          attach="material"
          distort={0.35}
          speed={2}
          roughness={0.2}
          metalness={0.8}
        />
      </Sphere>
    </Float>
  )
}

function Particles({ count = 400 }: { count?: number }) {
  const positions = useMemo(() => {
    const arr = new Float32Array(count * 3)
    for (let i = 0; i < count; i++) {
      arr[i * 3] = (Math.random() - 0.5) * 12
      arr[i * 3 + 1] = (Math.random() - 0.5) * 12
      arr[i * 3 + 2] = (Math.random() - 0.5) * 12
    }
    return arr
  }, [count])

  const ref = useRef<THREE.Points>(null)
  useFrame((state) => {
    if (ref.current) {
      ref.current.rotation.y = state.clock.elapsedTime * 0.02
      const mx = (state.pointer.x * Math.PI) / 8
      const my = (state.pointer.y * Math.PI) / 8
      ref.current.rotation.x = THREE.MathUtils.lerp(ref.current.rotation.x, my, 0.05)
      ref.current.rotation.z = THREE.MathUtils.lerp(ref.current.rotation.z, mx, 0.05)
    }
  })

  return (
    <points ref={ref}>
      <bufferGeometry>
        <bufferAttribute attach="attributes-position" args={[positions, 3]} />
      </bufferGeometry>
      <pointsMaterial size={0.03} color="#06b6d4" transparent opacity={0.8} />
    </points>
  )
}

export function HeroScene() {
  return (
    <div className="absolute inset-0 -z-10 h-full min-h-[520px] w-full">
      <Canvas camera={{ position: [0, 0, 5], fov: 50 }} dpr={[1, 2]} gl={{ antialias: true, alpha: true }}>
        <color attach="background" args={['#050816']} />
        <ambientLight intensity={0.4} />
        <pointLight position={[10, 10, 10]} intensity={1.2} color="#7c3aed" />
        <pointLight position={[-8, -4, 6]} intensity={0.8} color="#06b6d4" />
        <Stars radius={80} depth={40} count={1200} factor={3} fade speed={0.5} />
        <AiOrb />
        <Particles />
      </Canvas>
    </div>
  )
}
